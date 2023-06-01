package com.security.cors.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class ProjectConfig{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        /**
         * Customizer<CorsConfigurer> 객체를 매개변수로 받는 람다식
         *
         * 실제 예제에서는 CorsConfigurationSource 를 구현한 클래스를 만들고 주입 받아서 사용하는 것이 좋다.
         *  (유지 보수)
         */
        http.cors(c -> {
            CorsConfigurationSource source = request ->{
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(
                        List.of("example.com", "example.org"));
                config.setAllowedMethods(
                        List.of("GET","POST","PUT","DELETE"));

                return config;
            };
            c.configurationSource(source);
        });

        http.csrf().disable();
        return http.build();
    }


}
