package ex06.service;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * AuthenticationManager 에 직접 구현한 공급자를 주입한다. 이 클래스를 따로 분리한 이유는
 * 애플리케이션 구조상 시큐리티 설정 클래스에 넣게되면 공급자와 설정클래스간 양방향 참조가 발생하기 떄문이다.
 *
 * 공급자에서 사용하는 인코더와 설정클래스에서 빈으로 구현한 인코더간의 양방향 참조.
 *
 * 이 클래스는 단순히 매니저에 공급자를 주입하는 구성 설정 역할을 한다.
 */
@Configuration
public class CustomAuthenticationManager {

    private AuthenticationProvider authenticationProvider;

    public CustomAuthenticationManager(AuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authManagerBuilder
                = http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder.authenticationProvider(authenticationProvider);

        return authManagerBuilder.build();
    }
}
