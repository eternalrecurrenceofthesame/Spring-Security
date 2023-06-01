package com.spring.businesslogicserver.config.securityconfig;

import com.spring.businesslogicserver.authentication.UsernamePasswordAuthentication;
import com.spring.businesslogicserver.authentication.filter.InitialAuthenticationFilter;
import com.spring.businesslogicserver.authentication.filter.JwtAuthenticationFilter;
import com.spring.businesslogicserver.authentication.provider.OtpAuthenticationProvider;
import com.spring.businesslogicserver.authentication.provider.UsernamePasswordAuthenticationProvider;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@AllArgsConstructor
@Configuration
public class SecurityConfig {

    private InitialAuthenticationFilter initialAuthenticationFilter;
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        /**
         * 다른 출처를 이용할 때 적용되는 사항이 아니므로 csrf 보호를 비활성화 한다.
         * JWT 토큰을 이용해서 CSRF 토큰을 통한 검증을 대신한다.
         */
        http.csrf().disable();

        http.addFilterAt(initialAuthenticationFilter, BasicAuthenticationFilter.class)
                .addFilterAt(jwtAuthenticationFilter, BasicAuthenticationFilter.class);

        /**
         * 권한 부여 필터
         */
        http.authorizeRequests()
                .mvcMatchers(HttpMethod.POST,"/login")// 로그인 접근
                .permitAll()
                .anyRequest()
                .authenticated();


        return http.build();

    }
}
