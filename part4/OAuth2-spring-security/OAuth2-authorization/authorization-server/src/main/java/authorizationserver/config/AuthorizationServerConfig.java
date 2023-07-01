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
package authorizationserver.config;

import authorizationserver.federation.FederatedIdentityIdTokenCustomizer;
import authorizationserver.jose.Jwks;
import authorizationserver.web.authentication.DeviceClientAuthenticationConverter;
import authorizationserver.authentication.DeviceClientAuthenticationProvider;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.UUID;
/**
 * @author Joe Grandja
 * @author Daniel Garnier-Moiroux
 * @author Steve Riesenberg
 * @since 1.1
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent"; // OAuth2 로그인 동의 화면

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            RegisteredClientRepository registeredClientRepository,
            AuthorizationServerSettings authorizationServerSettings) throws Exception {

        // 인가 서버 설정을 기본 설정으로 한다.
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);


        /*
         * This sample demonstrates the use of a public client that does not
         * store credentials or authenticate with the authorization server.
         *
         * The following components show how to customize the authorization
         * server to allow for device clients to perform requests to the
         * OAuth 2.0 Device Authorization Endpoint and Token Endpoint without
         * a clientId/clientSecret.
         *
         * CAUTION: These endpoints will not require any authentication, and can
         * be accessed by any client that has a valid clientId.
         *
         * It is therefore RECOMMENDED to carefully monitor the use of these
         * endpoints and employ any additional protections as needed, which is
         * outside the scope of this sample.
         */

        /**
         * 디바이스 인가 논리 구현
         *
         * 디바이스 그랜트 타입은 웹 브라우저가 아닌 클라이언트 기기에서 권한 부여 서버에 인청 요청을 할 때 사용하는 그랜트 유형으로써
         * 리소스 서버에 접근하는 디바이스의 인가를 사용자가 웹 브라우저에서 진행하고 디바이스에 리소스 서버에 접근할 수 있는 토큰을 부여하는 방식
         *
         * https://pragmaticwebsecurity.com/img/articles/device-flow/deviceflow.png 그림 참고
         *
         * 디바이스 논리를 구현하기 위해 디바이스 컨버터를 사용해서 디바이스 토큰을 생성하는 설정을 추가한다.
         */
        DeviceClientAuthenticationConverter deviceClientAuthenticationConverter =
                new DeviceClientAuthenticationConverter(
                authorizationServerSettings.getDeviceAuthorizationEndpoint()); // authorizationEndpoint 설정 주입
        DeviceClientAuthenticationProvider deviceClientAuthenticationProvider =
                new DeviceClientAuthenticationProvider(registeredClientRepository);


        /**
         * 디바이스 그랜트 타입 Authorization 세부 설정
         */
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)

                .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint ->
                        deviceAuthorizationEndpoint.verificationUri("/activate"))
                .deviceVerificationEndpoint(deviceVerificationEndpoint ->
                        deviceVerificationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))

                .clientAuthentication(clientAuthentication ->
                        clientAuthentication
                                .authenticationConverter(deviceClientAuthenticationConverter)
                                .authenticationProvider(deviceClientAuthenticationProvider))
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))
                .oidc(Customizer.withDefaults()); // Enable OpenId Connect 1.0


        /**
         * 인가되지 않은 요청은 로그인으로 보낸다.
         */
        http
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .oauth2ResourceServer(oauth2ResourceServer ->
                        oauth2ResourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }



    /**
     * 클라이언트 유형을 등록한다.
     *
     * 클라이언트 유형이란 클라이언트 애플리케이션으로써 사용자의 대리인 역할을 하는 애플리케이션을 의미한다.
     * OAuth 2 프레임워크를 구현할 때 필요한 세 가지 애플리케이션 중 하나가 된다. (클라이언트, 인가, 리소스)
     *
     * 쉽게말해서 인가 서버에 등록된 클라이언트 정보를 알고있는 클라이언트 애플리케이션만 권한 부여 서버에 접근할 수 있게 된다.
     *
     * 사용자가 권한 부여 서버에서 사용자 인증에 성공하면 권한 부여 서버는 클라이언트 애플리케이션에 토큰 획득을 위한 승인 코드와 상태값을 제공한다.
     * 이때 사용되는 상태값이 권한 부여 서버에 등록된 클라이언트 정보이다.
     *
     * 클라이언트 애플리케이션은 상태 값이 클라이언트 애플리케이션에 등록된 설정과 같은지 검사하여 다른 사람이 클라이언트 애플리케이션의
     * 리디렉션 URI 를 호출하려는 것이 아닌지 확인할 수 있다.
     *
     * OIDC 를 사용하면 승인 흐름 완료후 받아오는 토큰 값에 ID 토큰 값이 추가된다.
     * (기본적인 클레임 외에 부가적인 값이 추가된다.)
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate){

        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("messaging-client")
                .clientSecret("{noop}secret") // noop 패스워드 인코더 오류 방지
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUri("http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc")
                .redirectUri("http://127.0.0.1:8080/authorized")
                .postLogoutRedirectUri("http://127.0.0.1:8080/logged-out")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("message.read")
                .scope("message.write")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build()) // 클라이언트에 인가 동의를 물어보는 옵션
                .build();


        // 디바이스로 인가 요청시 사용되는 클라이언트
        RegisteredClient deviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("device-messaging-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .scope("message.read")
                .scope("message.write")
                .build();

        // Save registered client's in db as if in-memory
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        registeredClientRepository.save(registeredClient);
        registeredClientRepository.save(deviceClient);

        return registeredClientRepository;
    }



    /**
     * 서명키 설정 및 토큰 디코더를 빈으로 등록
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(){
        RSAKey rsaKey = Jwks.generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return(jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     * 인가 서버 세팅 자바 설정
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }


    /**
     * 토큰 ID 값을 커스텀할 수 있는 빈을 등록
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer() {
        return new FederatedIdentityIdTokenCustomizer();
    }

    /**
     * 인가된 정보를 저장 삭제 조회할 수 있는 서비스
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate,
                                                           RegisteredClientRepository registeredClientRepository){
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate,
                                                                         RegisteredClientRepository registeredClientRepository) {

        // Will be used by the ConsentController
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * H 2 내장 데이터베이스를 사용해서 인가 정보를 저장할 수 있다.
     */
    @Bean
    public EmbeddedDatabase embeddedDatabase() {
        // @formatter:off
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .setScriptEncoding("UTF-8")
                .addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql")
                .addScript("org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql")
                .addScript("org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql")
                .build();
        // @formatter:on

    }

}
