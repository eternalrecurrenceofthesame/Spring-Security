package com.securty.csrfex3.config;

import com.securty.csrfex3.csrf.CustomCsrfTokenRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;

@Configuration
public class ProjectConfig {

    @Bean
    public CsrfTokenRepository customTokenRepository(){
        return new CustomCsrfTokenRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(c -> {  // 람다 인자로 CsrfConfigurer 을 사용한다
            c.csrfTokenRepository(customTokenRepository());
            c.ignoringAntMatchers("/ciao");

            /* 범용 방법들.
            HandlerMappingIntrospector i = new HandlerMappingIntrospector();
            MvcRequestMatcher r = new MvcRequestMatcher(i, "/ciao");
            c.ignoringRequestMatchers(r);

            String pattern = ".*[0-9].*";
            String httpMethod = HttpMethod.POST.name();
            RegexRequestMatcher r = new RegexRequestMatcher(pattern, httpMethod);
            c.ignoringRequestMatchers(c)
             */
        });
        http.authorizeRequests()
                .anyRequest().permitAll();

        return http.build();
    }
}
