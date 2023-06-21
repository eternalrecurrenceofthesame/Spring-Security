package ex06.config;

import ex06.entity.User;
import ex06.model.CustomUserDetails;
import ex06.service.AuthenticationProviderService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 인증 자바 설정
 */
@Configuration
@EnableWebSecurity //
public class AuthenticationConfig {


    /**
     * 인코더 두 종류 등록
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SCryptPasswordEncoder sCryptPasswordEncoder(){
        return new SCryptPasswordEncoder(16384, 8, 1, 32, 64);
    }



    /**
     * 시큐리티 필터 체인 설정 시큐리티 7 부터는 람다식을 사용한다.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .formLogin(formLogin -> formLogin.defaultSuccessUrl("/main", true));


        return http.build();
    }

}
