// src/main/java/com/yedam/ac/web/AcReciptApiController.java
package com.yedam.ac.web;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.web.dto.AcReceiptRow;

@RestController
public class AcReciptApiController {

    private static final Logger log = LoggerFactory.getLogger(AcReciptApiController.class);
    private final NamedParameterJdbcTemplate jdbc;
    public AcReciptApiController(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    /* ---------- utils ---------- */
    private static long nz(Long v) { return v == null ? 0L : v; }
    private static String nz(String v, String def) { return (v == null) ? def : v; }
    private static boolean looksLikeWildcard(String s){
        if(!StringUtils.hasText(s)) return false;
        return s.indexOf('%')>=0 || s.indexOf('_')>=0 || s.indexOf('*')>=0;
    }

    /* ---------- schema cache ---------- */
    private static final class Schema {
        String ORDERS, ORDERS_DETAIL, PRODUCT, PARTNER;
        String O_UNQ, O_PARTNER, O_DATE;
        String OD_CODE, OD_QTY, OD_PRICE, OD_SUPPLY, OD_VAT_RATE, OD_PROD_CODE;
        String P_NAME, P_UNIT, P_NAME_UPPER;
        String PN_NAME, PN_TEL, PN_BIZNO, PN_CEO, PN_ADDR_EXPR, PN_NAME_UPPER;
    }
    private final Schema S = new Schema();
    private final AtomicBoolean schemaReady = new AtomicBoolean(false);

    private String getCompanyCodeSafely() {
        try {
            Class<?> ctx = Class.forName("com.yedam.ac.util.CompanyContext");
            Object code = ctx.getMethod("getCompanyCode").invoke(null);
            return code != null ? String.valueOf(code) : null;
        } catch (Throwable ignore) {}
        return null;
    }
    private boolean objectExists(String name){
        String sql = """
            SELECT 1 FROM ALL_OBJECTS
             WHERE UPPER(OBJECT_NAME)=UPPER(:name)
               AND OBJECT_TYPE IN ('TABLE','VIEW','SYNONYM')
        """;
        return !jdbc.query(sql, new MapSqlParameterSource("name",name), (rs,i)->1).isEmpty();
    }
    private boolean colExists(String table, String col){
        String sql = """
            SELECT 1 FROM ALL_TAB_COLUMNS
             WHERE UPPER(TABLE_NAME)=UPPER(:t)
               AND UPPER(COLUMN_NAME)=UPPER(:c)
        """;
        return !jdbc.query(sql, new MapSqlParameterSource()
                .addValue("t",table).addValue("c",col), (rs,i)->1).isEmpty();
    }
    private String qualify(String raw, String cc){
        List<String> cands = new ArrayList<>();
        if (StringUtils.hasText(cc)) { cands.add(cc + "_" + raw); cands.add(cc + raw); }
        cands.add("ERP_" + raw); cands.add(raw);
        for(String c: cands) if(objectExists(c)) return c;
        return null;
    }
    private String firstExistingName(List<String> raws, String cc){
        for(String r: raws){ String q = qualify(r,cc); if(q!=null) return q; }
        return null;
    }
    private String firstCol(String t, String... cands){
        if(t==null) return null;
        for(String c: cands) if(colExists(t,c)) return c;
        return null;
    }
    private String nvlOrBlank(String expr){ return expr==null ? "''" : "NVL(" + expr + ", '')"; }
    private String addressExpr(String pt, String... cands){
        if(pt==null) return "''";
        for(String c: cands){
            if(("ADDRESS".equalsIgnoreCase(c) || "ADDR".equalsIgnoreCase(c)) && colExists(pt,c))
                return "NVL(p."+c+",'')";
        }
        String a1=null,a2=null;
        for(String c: cands){ if("ADDRESS1".equalsIgnoreCase(c)||"ADDR1".equalsIgnoreCase(c)) a1=c;
            if("ADDRESS2".equalsIgnoreCase(c)||"ADDR2".equalsIgnoreCase(c)) a2=c; }
        if(a1!=null && colExists(pt,a1)){
            if(a2!=null && colExists(pt,a2)) return "(NVL(p."+a1+",'')||' '||NVL(p."+a2+",''))";
            return "NVL(p."+a1+",'')";
        }
        return "''";
    }

    @PostConstruct
    public void initSchemaOnce(){
        if(schemaReady.get()) return;
        String cc = getCompanyCodeSafely();

        S.ORDERS        = firstExistingName(List.of("ORDERS","ORDER","TB_ORDERS","ORDER_MST"), cc);
        S.ORDERS_DETAIL = firstExistingName(List.of("ORDERS_DETAIL","ORDER_DETAIL","ORDER_DTL","TB_ORDER_DETAIL","ORDER_ITEM"), cc);
        S.PRODUCT       = firstExistingName(List.of("PRODUCT","PRODUCTS","TB_PRODUCT","ITEM","GOODS"), cc);
        S.PARTNER       = firstExistingName(List.of("PARTNER","PARTNERS","TB_PARTNER","CUSTOMER","CLIENT"), cc);

        S.O_UNQ     = firstCol(S.ORDERS, "ORDER_UNIQUE_CODE","ORDER_CODE","ORDERS_CODE","ORDER_NO");
        S.O_PARTNER = firstCol(S.ORDERS, "PARTNER_CODE","CUSTOMER_CODE","CLIENT_CODE");
        S.O_DATE    = firstCol(S.ORDERS, "ORDER_DATE","CREATE_DATE","CREATED_AT");

        S.OD_CODE      = firstCol(S.ORDERS_DETAIL, "ORDER_DETAIL_CODE","ORDER_ITEM_CODE","DETAIL_CODE","LINE_NO");
        S.OD_QTY       = firstCol(S.ORDERS_DETAIL, "ORDER_QTY","QUANTITY","QTY");
        S.OD_PRICE     = firstCol(S.ORDERS_DETAIL, "UNIT_PRICE","PRICE","UNITPRICE");
        S.OD_SUPPLY    = firstCol(S.ORDERS_DETAIL, "AMOUNT_SUPPLY","SUPPLY","LINE_AMOUNT","AMOUNT");
        S.OD_VAT_RATE  = firstCol(S.ORDERS_DETAIL, "VAT_RATE","PCT_VAT","TAX_RATE");
        S.OD_PROD_CODE = firstCol(S.ORDERS_DETAIL, "PRODUCT_CODE","ITEM_CODE","GOODS_CODE");

        S.P_NAME       = firstCol(S.PRODUCT, "PRODUCT_NAME","ITEM_NAME","GOODS_NAME","NAME");
        S.P_UNIT       = firstCol(S.PRODUCT, "UNIT_NAME","UNIT","UOM");
        S.P_NAME_UPPER = colExists(S.PRODUCT, "NAME_UPPER") ? "NAME_UPPER" : null;

        S.PN_NAME      = firstCol(S.PARTNER, "PARTNER_NAME","CUSTOMER_NAME","CLIENT_NAME","SUPPLIER_NAME","NAME");
        S.PN_TEL       = firstCol(S.PARTNER, "TEL","PHONE","PARTNER_PHONE","CONTACT");
        S.PN_BIZNO     = firstCol(S.PARTNER, "BUSINESS_NO","BIZ_NO","BIZNO","BUSINESSNO");
        S.PN_CEO       = firstCol(S.PARTNER, "CEO_NAME","CEO","REPRESENTATIVE");
        S.PN_ADDR_EXPR = addressExpr(S.PARTNER, "ADDRESS","ADDR","ADDRESS1","ADDRESS2","ADDR1","ADDR2");
        S.PN_NAME_UPPER= colExists(S.PARTNER, "NAME_UPPER") ? "NAME_UPPER" : null;

        if (Stream.of(S.ORDERS,S.ORDERS_DETAIL,S.PRODUCT,S.PARTNER,
                S.O_UNQ,S.O_PARTNER,S.O_DATE,S.OD_CODE,S.OD_QTY,S.OD_PROD_CODE,S.P_NAME,S.PN_NAME).anyMatch(Objects::isNull)) {
            log.warn("[ACRECIPT] schema resolution incomplete: {}", S);
        } else {
            schemaReady.set(true);
        }
    }

    /* ---------- main API ---------- */
    @GetMapping("/api/acrecipt")
    public Object search(
            @RequestParam(value="partnerName", required=false) String partnerName,
            @RequestParam(value="productName", required=false) String productName,
            @RequestParam(value="from", required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value="to", required=false)   @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value="limit", required=false) Integer limit
    ){
        if(!schemaReady.get()){
            return Map.of("error","Schema not resolved at startup. Check tables/columns.");
        }

        final boolean hasPartner = StringUtils.hasText(partnerName);
        final boolean hasProduct = StringUtils.hasText(productName);
        final boolean partnerPrefix = hasPartner && !looksLikeWildcard(partnerName);
        final boolean productPrefix = hasProduct && !looksLikeWildcard(productName);

        int lim = Math.min(Math.max(1, (limit==null?200:limit)), 1000);

        MapSqlParameterSource p = new MapSqlParameterSource();
        if(from!=null) p.addValue("fromStart", Timestamp.valueOf(from.atStartOfDay()));
        if(to!=null)   p.addValue("toNext",   Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        if(hasPartner){
            String v = partnerName.trim().toUpperCase();
            p.addValue("partnerKey", partnerPrefix? v+"%" : "%"+v+"%");
        }
        if(hasProduct){
            String v = productName.trim().toUpperCase();
            p.addValue("productKey", productPrefix? v+"%" : "%"+v+"%");
        }
        p.addValue("lim", lim);

        String supplyExpr = (S.OD_SUPPLY!=null)
                ? "NVL(od."+S.OD_SUPPLY+",0)"
                : "(NVL(od."+S.OD_PRICE+",0) * NVL(od."+S.OD_QTY+",0))";
        String vatRateExpr = (S.OD_VAT_RATE!=null) ? "NVL(od."+S.OD_VAT_RATE+",10)" : "10";

        String partnerLikeExpr = (S.PN_NAME_UPPER!=null)
                ? "p."+S.PN_NAME_UPPER+" LIKE :partnerKey"
                : "UPPER(p."+S.PN_NAME+") LIKE :partnerKey";
        String productLikeExpr = (S.P_NAME_UPPER!=null)
                ? "pr."+S.P_NAME_UPPER+" LIKE :productKey"
                : "UPPER(pr."+S.P_NAME+") LIKE :productKey";

        // ---- 단일 SELECT + Top-N(ROWNUM) 래핑 (FETCH FIRST 미사용) ----
        String inner =
            "SELECT /*+ FIRST_ROWS(" + lim + ") */\n" +
            "  od."+S.OD_CODE+"           AS id,\n" +
            "  NVL(p."+S.PN_NAME+",'')    AS partner_name,\n" +
            "  " + nvlOrBlank("p."+S.PN_TEL)   + " AS partner_phone,\n" +
            "  " + nvlOrBlank("p."+S.PN_BIZNO) + " AS business_no,\n" +
            "  " + nvlOrBlank("p."+S.PN_CEO)   + " AS ceo_name,\n" +
            "  " + S.PN_ADDR_EXPR               + " AS partner_address,\n" +
            "  NVL(pr."+S.P_NAME+",'')    AS product_name,\n" +
            "  " + nvlOrBlank("pr."+S.P_UNIT)  + " AS unit,\n" +
            "  NVL(od."+S.OD_QTY+",0)     AS qty,\n" +
            "  " + supplyExpr + "         AS amount_supply,\n" +
            "  " + vatRateExpr + "        AS pct_vat,\n" +
            "  o."+S.O_DATE+"             AS order_date\n" +
            "FROM "+S.ORDERS+" o\n" +
            "JOIN "+S.ORDERS_DETAIL+" od ON od."+S.O_UNQ+" = o."+S.O_UNQ+"\n" +
            "LEFT JOIN "+S.PARTNER+" p ON p."+S.O_PARTNER+" = o."+S.O_PARTNER+"\n" +
            "LEFT JOIN "+S.PRODUCT+" pr ON pr."+S.OD_PROD_CODE+" = od."+S.OD_PROD_CODE+"\n" +
            "WHERE 1=1\n" +
            (from!=null? "  AND o."+S.O_DATE+" >= :fromStart\n" : "") +
            (to!=null  ? "  AND o."+S.O_DATE+" <  :toNext\n"   : "") +
            (hasPartner? "  AND "+partnerLikeExpr+"\n"          : "") +
            (hasProduct? "  AND "+productLikeExpr+"\n"          : "") +
            "ORDER BY o."+S.O_DATE+" DESC, od."+S.OD_CODE+" ASC";

        String sql =
            "SELECT * FROM (\n" +
            inner + "\n" +
            ") WHERE ROWNUM <= :lim";

        try {
            List<AcReceiptRow> rows = jdbc.query(sql, p, new RowMapper<AcReceiptRow>() {
                @Override public AcReceiptRow mapRow(ResultSet rs, int rowNum) throws SQLException {
                    long supply = nz(rs.getLong("amount_supply"));
                    int pctVat  = rs.getInt("pct_vat");
                    long vat    = Math.round(supply * (pctVat / 100.0));
                    long total  = supply + vat;

                    java.util.Date orderDt = rs.getTimestamp("order_date");

                    return AcReceiptRow.builder()
                            .id(nz(rs.getString("id"), String.valueOf(rowNum + 1)))
                            .partnerName(nz(rs.getString("partner_name"), ""))
                            .partnerPhone(nz(rs.getString("partner_phone"), ""))
                            .businessNo(nz(rs.getString("business_no"), ""))
                            .partnerCeo(nz(rs.getString("ceo_name"), ""))
                            .partnerAddress(nz(rs.getString("partner_address"), ""))
                            .productName(nz(rs.getString("product_name"), ""))
                            .unit(nz(rs.getString("unit"), ""))
                            .qty(rs.getInt("qty"))
                            .amountSupply(supply)
                            .amountVat(vat)
                            .amountTotal(total)
                            .salesDate(orderDt)
                            .build();
                }
            });
            return rows;
        } catch (Exception e){
            log.warn("[ACRECIPT] Query failed: {}\n\n{}", e.getMessage(), e);
            return Map.of(
                "error","Query failed",
                "message", e.getMessage(),
                "sql", sql,
                "params", p.getValues()
            );
        }
    }
}
