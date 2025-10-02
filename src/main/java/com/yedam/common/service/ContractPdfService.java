package com.yedam.common.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import jakarta.annotation.PostConstruct;

@Service
public class ContractPdfService {

    @Value("${app.contract.base-dir:${user.home}/contracts}")
    private String baseDir;

    // 선택: 폰트 절대/상대 경로를 직접 지정하고 싶을 때
    @Value("${app.contract.font-path:}")
    private String fontPath;

    @PostConstruct
    public void ensureBaseDirOnStartup() {
        try {
            Files.createDirectories(Paths.get(baseDir));
        } catch (Exception e) {
            e.printStackTrace(); // 권한/경로 문제 확인용
        }
    }

    /**
     * classpath의 HTML 템플릿을 로드해 ${키} 치환 후 PDF "바이트"로 반환 (다운로드용)
     *
     * @param classpathHtml ex) "common/contract-pdf.html"
     * @param vars          ${키} -> 값 맵 (signatureDataUrl 같은 data:image/png;base64,... 포함 가능)
     * @return PDF bytes
     */
    public byte[] generateBytesFromTemplate(String classpathHtml, Map<String, String> vars) throws Exception {
        // ---------- 1) HTML 템플릿 찾기 ----------
        String fileName = classpathHtml;
        int slash = classpathHtml.lastIndexOf('/');
        if (slash >= 0) fileName = classpathHtml.substring(slash + 1);

        String[] htmlCandidates = new String[] {
                classpathHtml,                          // 호출자가 넣은 경로 (예: "common/contract-pdf.html")
                "templates/common/" + fileName,
                "common/" + fileName,
                "pdf/" + fileName,
                "static/" + fileName
        };

        String html = null;
        ClassPathResource htmlRes = null;
        for (String path : htmlCandidates) {
            ClassPathResource tryRes = new ClassPathResource(path);
            if (tryRes.exists()) {
                htmlRes = tryRes;
                break;
            }
        }

        if (htmlRes != null) {
            try (InputStream is = htmlRes.getInputStream()) {
                html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } else {
            // 개발 중엔 파일시스템에서 바로 읽을 수도 있게 보조 시도
            Path fsPath = Paths.get("src/main/resources/common", fileName);
            if (Files.exists(fsPath)) {
                html = Files.readString(fsPath, StandardCharsets.UTF_8);
            } else {
                throw new java.io.FileNotFoundException(
                        "Template not found. Tried: " + java.util.Arrays.toString(htmlCandidates)
                        + " and " + fsPath.toAbsolutePath());
            }
        }

        // ---------- 2) 변수 치환 ----------
        if (vars != null) {
            for (Map.Entry<String, String> e : vars.entrySet()) {
                html = html.replace("${" + e.getKey() + "}", e.getValue() == null ? "" : e.getValue());
            }
        }

        // ---------- 3) baseUrl (HTML 내 상대경로 처리용) ----------
        String baseUrlStr = null;
        ClassPathResource staticRoot = new ClassPathResource("static/");
        if (staticRoot.exists()) {
            URL baseUrl = staticRoot.getURL();
            baseUrlStr = baseUrl.toExternalForm();
        }

        // ---------- 4) 폰트 로딩(여러 경로 시도 + 설정값) ----------
        byte[] fontBytes = null;

        // classpath 후보
        String[] fontCandidates = new String[] {
                "static/erp/fonts/NanumGothic.ttf",
                "static/fonts/NanumGothic.ttf",
                "fonts/NanumGothic.ttf",
                "templates/common/fonts/NanumGothic.ttf",
                "common/fonts/NanumGothic.ttf"
        };
        for (String p : fontCandidates) {
            ClassPathResource fr = new ClassPathResource(p);
            if (fr.exists()) {
                try (InputStream fis = fr.getInputStream()) {
                    fontBytes = fis.readAllBytes();
                }
                break;
            }
        }

        // 파일시스템 후보(개발 환경)
        if (fontBytes == null) {
            Path[] fsFonts = new Path[] {
                    Paths.get("src/main/resources/static/erp/fonts/NanumGothic.ttf"),
                    Paths.get("src/main/resources/static/fonts/NanumGothic.ttf"),
                    Paths.get("src/main/resources/fonts/NanumGothic.ttf"),
                    Paths.get("src/main/resources/templates/common/fonts/NanumGothic.ttf"),
                    Paths.get("src/main/resources/common/fonts/NanumGothic.ttf")
            };
            for (Path p : fsFonts) {
                if (Files.exists(p)) {
                    fontBytes = Files.readAllBytes(p);
                    break;
                }
            }
        }

        // 설정값으로 직접 지정
        if (fontBytes == null && fontPath != null && !fontPath.isBlank()) {
            Path p = Paths.get(fontPath);
            if (Files.exists(p)) {
                fontBytes = Files.readAllBytes(p);
            }
        }

        // ---------- 5) PDF 바이트 생성 ----------
        try (var baos = new java.io.ByteArrayOutputStream()) {
            PdfRendererBuilder b = new PdfRendererBuilder();

            final byte[] fontBytesFinal = fontBytes;
            if (fontBytesFinal != null && fontBytesFinal.length > 0) {
                b.useFont(() -> new ByteArrayInputStream(fontBytesFinal), "NanumGothic");
            } else {
                System.err.println("[WARN] NanumGothic.ttf not found. PDF will use default fonts.");
            }

            b.withHtmlContent(html, baseUrlStr); // baseUrl 없으면 null 가능
            b.useFastMode();
            b.toStream(baos);
            b.run();

            return baos.toByteArray();
        }
    }

    // (선택) 파일 저장 버전이 필요하면 기존 generateFromTemplate(...) 유지 가능
    public Path generateFromTemplate(String classpathHtml, Map<String, String> vars, Path outDir, String outFileName)
            throws Exception {
        byte[] pdf = generateBytesFromTemplate(classpathHtml, vars);

        if (outDir == null) outDir = Paths.get(baseDir);
        Files.createDirectories(outDir);

        if (outFileName == null || outFileName.isBlank()) {
            String orderId = (vars == null) ? null : vars.getOrDefault("orderId", "");
            String safeOrderId = makeSafeFilePart(orderId);
            String ts = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            outFileName = (safeOrderId.isEmpty())
                    ? "contract_" + ts + ".pdf"
                    : "contract_" + safeOrderId + "_" + ts + ".pdf";
        }
        Path outPath = outDir.resolve(outFileName);

        try (OutputStream os = Files.newOutputStream(outPath)) {
            os.write(pdf);
        }
        return outPath;
    }

    /** 파일명 안전화(윈도/리눅스 특수문자 제거) */
    private static String makeSafeFilePart(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        String safe = trimmed
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_")
                .replaceAll("[\\p{Cntrl}]", "");
        if (safe.length() > 80) safe = safe.substring(0, 80);
        return safe;
    }
}
