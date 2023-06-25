package workoutresourceserver.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 리소스 서버 시큐리티 구성 클래스
 */
@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity // @PreAuthorize, @PostAuthorize 애노테이션과 SpEL 식으로 권한 부여 규칙을 지정할 수 있게 해준다.
@EnableWebSecurity
public class ResourceServerConfig {

    public static final String ADMIN = "admin";
    public static final String USER = "user";

    private final JwtAuthConverter jwtAuthConverter;

    /**
     * 시큐리티 설정
     *
     * DELETE 는 admin 만 호출할 수 있다. 그외 다른 요청들도 인증된 클라이언트만 접근할 수 있다.
     *
     * .oauth2ResourceServer 메서드로 jwkUri 를 설정해서 필터 체인으로 디코더를 설정하거나
     * 직접 디코더를 만들어서 사용할 수 있다.
     *
     * https://github.com/eternalrecurrenceofthesame/Spring-security-in-Action/tree/main/OAuth2-spring-security/OAuth2-resource 참고
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers(HttpMethod.DELETE, "/**").hasRole(ADMIN)
                .anyRequest().authenticated();
        http.oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtAuthConverter);

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    /**
     * 스프링 데이터에서 JPQL 에 SpEL 식을 사용하기 위해 빈을 만들었다.
     */
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension(){
        return new SecurityEvaluationContextExtension();
    }


}
