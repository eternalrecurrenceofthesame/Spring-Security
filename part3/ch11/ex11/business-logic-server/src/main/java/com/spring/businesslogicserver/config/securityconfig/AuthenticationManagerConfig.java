package com.spring.businesslogicserver.config.securityconfig;

import com.spring.businesslogicserver.authentication.provider.OtpAuthenticationProvider;
import com.spring.businesslogicserver.authentication.provider.UsernamePasswordAuthenticationProvider;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 필터에서 시큐리티 설정에 만들어진 인증 관리자를 참조하고 실큐리티 설정에서 필터를 참조하면
 * 필터와 시큐리티 설정간 순환 참조가 걸린다.
 *
 * 인증 관리자를 따로 빼서 사용한다. (리팩토링 예정 응집성의 문제)
 */
@AllArgsConstructor
@Configuration
public class AuthenticationManagerConfig {

    private OtpAuthenticationProvider otpAuthenticationProvider;
    private UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider;


    /**
     * 인증 관리자 등록
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        /**
         * 인증 매니저에 필터 추가시 순서대로 공급자를 호출하기 때문에 먼저 호출할 공급자를 뒤에 등록하자!
         */
        authManagerBuilder
                .authenticationProvider(otpAuthenticationProvider)
                .authenticationProvider(usernamePasswordAuthenticationProvider);


        return authManagerBuilder.build();
    }

}
