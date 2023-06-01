package com.spring.singlesignon.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ProjectConfig {

    /**
     * 메모리 저장소에 ClientRegistration (연결된 인증 서버) 을 저장한다.
     */
    @Bean
    public ClientRegistrationRepository clientRepository(){
        var c = clientRegistration();
        return new InMemoryClientRegistrationRepository(c);
    }

    /**
     * CommonOAuth2Provider 정의하기 매우 간편하다!
     */
    private ClientRegistration clientRegistration(){
        return CommonOAuth2Provider.GITHUB.getBuilder("github")
                .clientId("")
                .clientSecret("")
                .build();
    }

    /**
     * ClientRegistration 정의하기
     */
    /*
    private ClientRegistration clientRegistration(){
        ClientRegistration cr = ClientRegistration.withRegistrationId("github")
                .clientId("")
                .clientSecret(" ")
                .scope(new String[]{"read:user"})
                .authorizationUri("https://github.com/login/oauth/authorize")
                .tokenUri("https://github.com/login/oauth/access_token")
                .userInfoUri("https://api.github.com.user")
                .clientName("GitHub")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}") // deprecated 됐지만 예제 진행을 위해 사용했음.
                .build();

                 return cr;
    }

     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /**
         * 인증 서버 등록 방법 2 인증 필터에서 직접 등록할 수도 있다.
         */
        /*
        http.oauth2Login(c -> {
            c.clientRegistrationRepository(clientRepository());
        }) ;
         */

        http.oauth2Login();

        http.authorizeRequests()
                .anyRequest()
                .authenticated();
        return http.build();
    }
}
