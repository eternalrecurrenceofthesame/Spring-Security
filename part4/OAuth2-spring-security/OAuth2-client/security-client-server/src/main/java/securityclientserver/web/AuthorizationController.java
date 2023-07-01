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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * @author Joe Grandja
 * @since 0.0.1
 */

/**
 * 승인 코드 그랜트 유형을 사용할 때 호출하는 API
 */
@Controller
public class AuthorizationController {

    private final WebClient webClient;
    private final String messagesBaseUri;

    /**
     * reactive WebClient 와 리소스 서버 엔드포인트를 주입 받는다.
     */
    public AuthorizationController(WebClient webClient,
                                   @Value("${messages.base-uri") String messagesBaseUri){
        this.webClient = webClient;
        this.messagesBaseUri = messagesBaseUri;
    }


    /**
     * 클라이언트에서 승인 코드 그랜트 유형을 사용해서 리소스를 호출하는 엔드포인트
     */
    @GetMapping(value = "/authorize", params = "grant_type=authorization_code")  // /authorize?grant_type=authorization_code
    public String authorizationCodeGrant(Model model,
                                         @RegisteredOAuth2AuthorizedClient("messaging-client-authorization-code")
                                         OAuth2AuthorizedClient authorizedClient){

        String[] messages = this.webClient
                .get()
                .uri(this.messagesBaseUri)
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
                .retrieve()
                .bodyToMono(String[].class)
                .block();

        model.addAttribute("messages", messages);

        return "index";
    }

    // '/authorized' is the registered 'redirect_uri' for authorization_code - yml 참고
    // 인가에 성공하고 리다이렉트 uri 가 호출될 때 오류가 발생하면 이 엔드포인트가 호출되는듯
    @GetMapping(value = "/authorized", params = OAuth2ParameterNames.ERROR)
    public String authorizationFailed(Model model, HttpServletRequest request){

        String errorCode = request.getParameter(OAuth2ParameterNames.ERROR);

        if(StringUtils.hasText(errorCode)){
            model.addAttribute("error",
                    new OAuth2Error(
                            errorCode,
                            request.getParameter(OAuth2ParameterNames.ERROR_DESCRIPTION),
                            request.getParameter(OAuth2ParameterNames.ERROR_URI)
                    ));
        }

        return "index";
    }

    @GetMapping(value = "/authorize", params = "grant_type=client_credentials")
    public String clientCredentialsGrant(Model model){

        String[] messages = this.webClient
                .get()
                .uri(this.messagesBaseUri)
                .attributes(ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId("messaging-client-client-credentials"))
                .retrieve()
                .bodyToMono(String[].class)
                .block();

        model.addAttribute("messages", messages);

        return "index";
    }

    @GetMapping(value = "/authorize", params = "grant_type=device_code")
    public String deviceCodeGrant() {
        return "device-activate";
    }

    @ExceptionHandler(WebClientResponseException.class)
    public String handleError(Model model, WebClientResponseException ex){
        model.addAttribute("error", ex.getMessage());

        return "index";
    }

}
