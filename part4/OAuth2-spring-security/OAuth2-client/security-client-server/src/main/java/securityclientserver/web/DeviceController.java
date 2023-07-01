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
package securityclientserver.web;

import ch.qos.logback.core.net.server.Client;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;

/**
 * @author Steve Riesenberg
 * @since 1.1
 */
@Controller
public class DeviceController {

    private static final Set<String> DEVICE_GRANT_ERRORS = new HashSet<>(Arrays.asList(
            "authorization_pending",
            "slow_down",
            "access_denied",
            "expired_token"
    ));

    /**
     * In order to capture the generic type and retain it at runtime
     */
    private static final ParameterizedTypeReference<Map<String, Object>> TYPE_REFERENCE =
            new ParameterizedTypeReference<>(){};

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final WebClient webClient;

    private final String messagesBaseUri;

    public DeviceController(ClientRegistrationRepository clientRegistrationRepository, WebClient webClient,
                            @Value("${messages.base-uri}") String messagesBaseUri){

        this.clientRegistrationRepository = clientRegistrationRepository;
        this.webClient = webClient;
        this.messagesBaseUri = messagesBaseUri;
    }

    /**
     * 디바이스 클라이언트에서 권한부여 서버에 액세스 토큰을 요청하는 API
     */
    @GetMapping("/device-authorize")
    public String authorize(Model model){

        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId("messaging-client-device-code");

        /**
         * LinkedMultiValueMap 은 스프링이 제공하는 기능으로 하나의 키에 복수 값을 사용할 수 있게끔 해준다.
         *
         * collectionToDelimitedString 은 컬렉션에 delimiter(구분자) 를 넣어준다 SCOPE 를 가져와서 구분자를 포함해서 하나로 이어준다.
         */
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
        requestParameters.add(OAuth2ParameterNames.SCOPE, StringUtils.collectionToDelimitedString(clientRegistration.getScopes(), " "));


        String deviceAuthorizationUri = (String) clientRegistration.getProviderDetails().getConfigurationMetadata().get("device_authorization_endpoint");

        /**
         * 권한 부여 서버에 디바이스 그랜트 flow 를 시작한다. 디바이스 클라이언트 인증을 요청하면서
         * 액세스 토큰 값을 얻을 때 필요한 디바이스 코드, 유저 코드 등등 을 함께 요청한다.
         */
        Map<String, Object> responseParameters = this.webClient.post()
                .uri(deviceAuthorizationUri)
                .headers(headers -> {
                    /*
                     * This sample demonstrates the use of a public client that does not
                     * store credentials or authenticate with the authorization server.
                     *
                     * See DeviceClientAuthenticationProvider in the authorization server
                     * sample for an example customization that allows public clients.
                     *
                     * For a confidential client, change the client-authentication-method to
                     * client_secret_basic and set the client-secret to send the
                     * OAuth 2.0 Device Authorization Request with a clientId/clientSecret.
                     */
                    if (!clientRegistration.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {
                        headers.setBasicAuth(clientRegistration.getClientId(), clientRegistration.getClientSecret());
                    }
                })
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(requestParameters))
                .retrieve()
                .bodyToMono(TYPE_REFERENCE)
                .block();

        Objects.requireNonNull(responseParameters, "Device Authorization Response cannot be null");

        Instant issuedAt = Instant.now();
        Integer expiresIn = (Integer) responseParameters.get(OAuth2ParameterNames.EXPIRES_IN);
        Instant expiresAt = issuedAt.plusSeconds(expiresIn); // 만료시간 계산

        /**
         * deviceCode, expiresAt, userCode, verificationUri, verificationUriComplete 를 포함해서
         * /device-authorize 템플릿을 호출한다.
         *
         * 이 템플릿은 자바 스크립트를 사용해서 사용자가 verificationUri 에서 userCode 를 사용해서 인가에 성공할 때까지
         * 반복적으로 액세스 토큰을 요청한다.
         */
        model.addAttribute("deviceCode", responseParameters.get(OAuth2ParameterNames.DEVICE_CODE));
        model.addAttribute("expiresAt", expiresAt);
        model.addAttribute("userCode", responseParameters.get(OAuth2ParameterNames.USER_CODE));
        model.addAttribute("verificationUri", responseParameters.get(OAuth2ParameterNames.VERIFICATION_URI));

        // Note: You could use a QR-code to display this URL
        model.addAttribute("verificationUriComplete", responseParameters.get(
                OAuth2ParameterNames.VERIFICATION_URI_COMPLETE));

        return "device-authorize";
    }

    /**
     * @see # handleError(OAuth2AuthorizationException)
     *
     * /device-authorize 템플릿에서 자바 스크립트가 poll 하는 엔드포인트.
     * DeviceCodeOAuth2AuthorizedClientProvider 를 거쳐서 액세스 토큰값 요청을 poll 한다.
     *
     * OAuth2AuthorizedClient 값을 주입 받아서 사용하면 OAuth2AuthorizedClientManager 를 거치는 로직이 적용되는 것 같다
     */
    @PostMapping("/device_authorize")
    public ResponseEntity<Void> poll(@RequestParam(OAuth2ParameterNames.DEVICE_CODE) String deviceCode,
                                     @RegisteredOAuth2AuthorizedClient("messaging-client-device-code")
                                     OAuth2AuthorizedClient authorizedClient){

        /*
         * The client will repeatedly poll until authorization is granted.
         *
         * The OAuth2AuthorizedClientManager uses the device_code parameter
         * to make a token request, which returns authorization_pending until
         * the user has granted authorization.
         *
         * If the user has denied authorization, access_denied is returned and
         * polling should stop.
         *
         * If the device code expires, expired_token is returned and polling
         * should stop.
         *
         * This endpoint simply returns 200 OK when the client is authorized.
         */

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @ExceptionHandler(OAuth2AuthorizationException.class)
    public ResponseEntity<OAuth2Error> handleError(OAuth2AuthorizationException ex) {

        String errorCode = ex.getError().getErrorCode();
        if (DEVICE_GRANT_ERRORS.contains(errorCode)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getError());
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getError());
    }

    /**
     * 리소스 요청 API
     */
    @GetMapping("/device_authorized")
    public String authorized(Model model,
                             @RegisteredOAuth2AuthorizedClient("messaging-client-device-code")
                             OAuth2AuthorizedClient authorizedClient){

        String[] messages = this.webClient.get()
                .uri(this.messagesBaseUri)
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String[].class)
                .block();
        model.addAttribute("messages", messages);

        return "index";
    }


}
