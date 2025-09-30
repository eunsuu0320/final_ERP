package com.yedam.common;

import java.io.Serializable;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public class SequenceIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        String prefix = "GEN-";            // 기본 접두어
        String sequenceName = "GEN_SEQ";   // 기본 시퀀스 이름

        // 엔티티가 Prefixable 인터페이스를 구현했다면 엔티티 설정 사용
        if (object instanceof Prefixable prefixable) {
            prefix = prefixable.getPrefix();
            sequenceName = prefixable.getSequenceName();
        }

        // 오라클 시퀀스 호출
        Long seq = ((Number) session
                .createNativeQuery("SELECT " + sequenceName + ".NEXTVAL FROM dual")
                .getSingleResult())
                .longValue();

        // 접두어 + 6자리(000001) 형식으로 반환
        return prefix + String.format("%04d", seq);
    }
}
