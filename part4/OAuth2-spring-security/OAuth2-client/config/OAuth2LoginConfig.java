package com.spring.oauth2client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrations;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class OAuth2LoginConfig {

    /**
     * 인증 서버를 등록하는 리포지토리
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(){
        return new InMemoryClientRegistrationRepository(this.googleClientRegistration());
    }

    /**
     * 인증 서버 정의 (구글)
     *
     *  보통 여기 있는 내용을 다 사용하지 않는다.. 직접 인증 서버를 설정한다면 이 녀석들을 참고하자!
     * 토큰 주소, 유저 정보 주소, 유저 이름, jwk 주소는 커스텀 인증 서버를 사용할 때 쓰는듯?
     */
    private ClientRegistration googleClientRegistration(){
        return ClientRegistration.withRegistrationId("google")
                .clientId("")
                .clientSecret("")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:8080/login/oauth2/code/google") // 승인 코드를 받는 리다이렉트 Uri
                .scope("openid") // 구글에서 제공하는 사용자 정보 범위
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth") // 로그인 화면
                .tokenUri("https://www.googleapis.com/oauth2/v4/token") // 토큰 주소
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo") // 유저 정보 주소
                .userNameAttributeName(IdTokenClaimNames.SUB) // 유저 이름
                .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs") // jwk 주소
                .clientName("Google") // 클라이언트 이름 설정
                .build();

    }


    /**
     * 인증된 클라이언트(사용자) 를 저장하는 저장소와 이것을 관리하는 서비스 스프링 부트가 기본적으로 제공해주지만
     * 구현해서 사용할 수도 있다.
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }


    /**
     * 저장소에 저장된 인증 클라이언트를 전반적으로 관리하는 관리자, 인증 요청에 대한 인증 또는 재인증을 시도한다.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository){

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode() // 승인 코드 그랜트에서 액세스 토큰을 받을 때 사용하는 코드
                .clientCredentials()
                .password()
                .refreshToken()
                .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository,
                authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);


        /**
         * username, password 값이 HttpServletRequest 파라미터로 넘어온다면 이 값을
         * DefaultOAuth2AuthorizedClientManager 가 가지는 ContextAttributesMapper 에 매핑해서 저장할 수 있다.
         *
         * 이 값은 PasswordOAuth2AuthorizedClientProvider(password grant 에서 사용됨) 가 사용한다.
         */
        authorizedClientManager.setContextAttributesMapper(contextAttributesMapper());

        return authorizedClientManager;
    }

    private Function<OAuth2AuthorizeRequest, Map<String, Object>> contextAttributesMapper() {
        return authorizeRequest -> {
            Map<String, Object> contextAttributes = Collections.emptyMap();
            HttpServletRequest servletRequest = authorizeRequest.getAttribute(HttpServletRequest.class.getName());
            String username = servletRequest.getParameter(OAuth2ParameterNames.USERNAME);
            String password = servletRequest.getParameter(OAuth2ParameterNames.PASSWORD);
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                contextAttributes = new HashMap<>();

                // `PasswordOAuth2AuthorizedClientProvider` requires both attributes
                contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username);
                contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password);
            }
            return contextAttributes;
        };
    }


}
