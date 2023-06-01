package com.spring.oauth2client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

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
     *  보통 여기 있는 내용을 다 사용하지 않는다.. 직접 인증 서버를 만든다면 이 녀석들을 참고하자!
     *
     *  토큰 주소, 유저 정보 주소, 유저 이름, jwk 주소는 커스텀 인증 서버를 사용할 때 쓰는듯?
     */
    private ClientRegistration googleClientRegistration(){
        return ClientRegistration.withRegistrationId("google")
                .clientId("")
                .clientSecret("")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
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
     * 액세스 토큰을 인증 서버와 리소스 서버에 연결하는 역할을 한다.
     * associating an Access Token credential
     */
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    /**
     * 액세스 토큰 저장소라고 생각하면 될듯?
     */
    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository(
            OAuth2AuthorizedClientService authorizedClientService) {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService);
    }


}
