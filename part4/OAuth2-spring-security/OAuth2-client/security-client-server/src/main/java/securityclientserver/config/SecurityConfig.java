/*
 * Copyright 2020-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package securityclientserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * @author Joe Grandja
 * @author Dmitriy Dubson
 * @author Steve Riesenberg
 * @since 0.0.1
 */

/**
 * 클라이언트 서버는 사용자(웹 브라우저) 의 대리인 역할 또는 디바이스가 된다.
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    /**
     * WebSecurityCustomizer 를 사용해서 제외 패턴을 지정한다.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring().requestMatchers("/webjars/**", "assets/**");
    }


    /**
     * 클라이언트 서버 시큐리티 필터 체인 설정
     *
     * 기본적으로 클라이언트 서버는 OAuth2Login 을 사용한다. OAuth2Login 을 관리하는 주체는 권한 부여 서버가 된다.
     * 즉 클라이언트에서 로그 아웃을 제외한 다른 요청을 하면 /oauth2/authorization/messaging-client-oidc 페이지로 이동되는데
     *
     * 이때 yml 설정으로 등록한 클라이언트 유형인 messaging-client-oidc 를 요청에 포함해서 권한부여 서버의 로그인 페이지를
     * 호출한다.
     *
     * 이 과정은 클라언트가 사용자를 권한부여서버의 로그인 페이지로 리다이렉트 할 때 액세스 토큰을 얻기위한 승인 코드와 상태값을
     * 포함해서 권한부여 서버에 요청을 하는 것이며 사용자 인증에 성공하면 권한 부여 서버는 사용자 인증 요청에 대한 코드와 상태값을 제공한다.
     *
     * 클라이언트는 승인 코드와 상태값을 사용해서 권한 부여 서버에 액세스 토큰을 요청할 수 있게 된다.
     * https://github.com/eternalrecurrenceofthesame/Spring-security-in-action/tree/main/part4/ch12 참고
     *
     * 참고로 이 설정은 클라이언트 애플리케이션에 구현한 디바이스 설정과는 상관 없다. 디바이스 그랜트 유형은 디바이스(애플 티비)
     * 애플리케이션에서 별도의 Authentication 과정을 거치지 않고 직접 토큰을 요청할 수 있기 떄문이다.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/logged-out").permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(oauth2Login ->
                        oauth2Login.loginPage("/oauth2/authorization/messaging-client-oidc"))
                .oauth2Client(Customizer.withDefaults())
                .logout(logout ->
                        logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)));

                return http.build();
    }

    /**
     * 로그아웃 성공핸들러
     */
    private LogoutSuccessHandler oidcLogoutSuccessHandler(
            ClientRegistrationRepository clientRegistrationRepository) {

        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        // Set the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        //
        // 클라이언트 애플리케이션에서 로그아웃 한 후 공급자에서 리다이렉트 시킬 Url 을 지정한다.
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/logged-out");

        return oidcLogoutSuccessHandler;
    }
}
