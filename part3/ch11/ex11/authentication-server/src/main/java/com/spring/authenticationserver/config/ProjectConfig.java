package com.spring.authenticationserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 인증 서버 구성 정보
 */
@Configuration
public class ProjectConfig {

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable(); // 애플리케이션의 모든 엔드포인트를 직접 실행할 수 있게 CSRF 비활성화

        http.authorizeRequests()
                .anyRequest().permitAll(); // 인증 없이 모든 호출 허용

        return http.build();
    }
}
