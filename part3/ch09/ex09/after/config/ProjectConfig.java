package springsecurity.ssia.ch9.after.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import springsecurity.ssia.ch9.after.config.filter.AuthenticationLoggingFilter;
import springsecurity.ssia.ch9.previous.config.filter.RequestValidationFilter;

@Configuration
public class ProjectConfig {

    /**
     * 간단하게 인메모리 사용자 세부 정보 서비스를 만듦
     */
    @Bean
    public UserDetailsService userDetailsService() {
        var manager = new InMemoryUserDetailsManager();

        var user1 = User.withUsername("john")
                .password("12345")
                .roles("PREMIUM") // 역할 부여
                .build();

        var user2 = User.withUsername("jane")
                .password("12345")
                .roles("MANAGER") // 역할 부여
                .build();

        manager.createUser(user1);
        manager.createUser(user2);

        return manager;
    }

    /**
     * 실습 진행을 위해 만든 패스워드 인코더
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }


    /**
     * 권한 부여 필터 + 필터 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(
                        new RequestValidationFilter(),
                        BasicAuthenticationFilter.class)
                .addFilterAfter(
                        new AuthenticationLoggingFilter(),
                        BasicAuthenticationFilter.class)
                .authorizeRequests()
                .anyRequest().permitAll();

        return http.build();
    }

}