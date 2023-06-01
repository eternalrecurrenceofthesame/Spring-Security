package springsecurity.ssia.ch5.mode_thredlocal.authentication;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * 인증 관리자를 프로젝트 구성에 넣게되면 프로바이더와 프로적트 구성 간에 양방향 참조가
 * 걸리기 때문에 따로 커스텀 프로젝트 매니저를 만들었다.
 *
 * 뭔가 어플리케이션의 응집성이 떨어져서 마음에 안 들지만 일단 실습 진행을 위해 유지.
 */
@AllArgsConstructor
//@Configuration
public class CustomAuthenticationManager {

    /**
     * 인증 공급자
     */
    private AuthenticationProvider authenticationProvider;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authManagerBuilder
                = http.getSharedObject(AuthenticationManagerBuilder.class);

        authManagerBuilder.authenticationProvider(authenticationProvider);

        return authManagerBuilder.build();
    }

}
