package com.spring.businesslogicserver.authentication;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class OtpAuthentication extends UsernamePasswordAuthentication{

    /**
     * 매개변수가 두 개인 생성자를 호출하면 인증 상태가 false
     * 매개변수가 세 개인 생성자를 호출하면 인증 상태를 true 로 반환한다.
     *
     * Authentication 객체가 인증 상태로 설정 되었다는 것은 인증 프로세스가 완료됐음을 의미한다.
     */
    public OtpAuthentication(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public OtpAuthentication(Object principal, Object credentials) {
        super(principal, credentials);
    }
}
