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
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import securityclientserver.authorization.DeviceCodeOAuth2AuthorizedClientProvider;

/**
 * @author Joe Grandja
 * @author Steve Riesenberg
 * @since 0.0.1
 */
@Configuration
public class WebClientConfig {

    /**
     * ServletOAuth2AuthorizedClientExchangeFilterFunction -> OAuth2AuthorizedClientManager -> OAuth2AuthorizedClientProvider
     *
     * WebClient 를 사용할 때 OAuth2AuthorizedClientManager 를 커스텀한 설정을 사용한다.
     *
     * OAuth2AuthorizedClient 로 액세스 토큰을 얻고 ServletOAuth2AuthorizedClientExchangeFilterFunction 를 적용한다.
     * 이 필터는 커스텀 DeviceCodeOAuth2AuthorizedClientProvider 를 주입 받아서 디바이스 요청시 사용되는 OAuth2AuthorizedClient 를 만들어준다.
     *
     */
    @Bean
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        // @formatter:off
        return WebClient.builder()
                .apply(oauth2Client.oauth2Configuration())
                .build();
        // @formatter:on
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        // @formatter:off
        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .provider(new DeviceCodeOAuth2AuthorizedClientProvider())
                        .build();
        // @formatter:on

        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        // Set a contextAttributesMapper to obtain device_code from the request
        authorizedClientManager.setContextAttributesMapper(DeviceCodeOAuth2AuthorizedClientProvider
                .deviceCodeContextAttributesMapper());

        return authorizedClientManager;
    }
}
