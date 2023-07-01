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
package authorizationserver.web;

import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.*;

/**
 * @author Daniel Garnier-Moiroux
 */
@Controller
public class AuthorizationConsentController {

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationConsentService auth2AuthorizationConsentService;

    public AuthorizationConsentController(RegisteredClientRepository registeredClientRepository, OAuth2AuthorizationConsentService auth2AuthorizationConsentService) {
        this.registeredClientRepository = registeredClientRepository;
        this.auth2AuthorizationConsentService = auth2AuthorizationConsentService;
    }

    /**
     * Authorization 동의 화면을 보여주는 엔드 포인트 화면에서 스코프 제공에 동의하면 액세스 토큰이
     * 포함된 Authorization 이 생성되고 Authorization_consent 값이 저장된다. (템플릿에서 자바 스크립트로 호출)
     */
    @GetMapping(value = "/oauth2/consent")
    public String consent(Principal principal, Model model,
                          @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
                          @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
                          @RequestParam(OAuth2ParameterNames.STATE) String state,
                          @RequestParam(name = OAuth2ParameterNames.USER_CODE, required = false) String userCode){

        // Remove scopes that were already approved
        Set<String> scopesToApprove = new HashSet<>();
        Set<String> previouslyApprovedScopes = new HashSet<>();
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        OAuth2AuthorizationConsent currentAuthorizationConsent = this.auth2AuthorizationConsentService.findById(registeredClient.getId(), principal.getName());

        Set<String> authorizedScopes;
        if(currentAuthorizationConsent != null){
            authorizedScopes = currentAuthorizationConsent.getScopes();
        }else{
            authorizedScopes = Collections.emptySet();
        }

        /**
         * delimited 를 사용해서 scope 를 구분한다. delimitedListToStringArray 는 연결된 스트링 값을
         * " " 띄어쓰기 기준으로 String 값으로 나누어 준다.
         */
        for (String requestedScope : StringUtils.delimitedListToStringArray(scope, " ")){
            if(OidcScopes.OPENID.equals(requestedScope)){ // 스코프 꺼내기
                continue; // openid 는 continue 한다.
            }
            if(authorizedScopes.contains(requestedScope)){
                previouslyApprovedScopes.add(requestedScope);
            }else{
                scopesToApprove.add(requestedScope);
            }

            model.addAttribute("clientId", clientId);
            model.addAttribute("state", state);
            model.addAttribute("scopes", withDescription(scopesToApprove));
            model.addAttribute("previouslyApprovedScopes", withDescription(previouslyApprovedScopes));
            model.addAttribute("principalName", principal.getName());
            model.addAttribute("userCode", userCode);

            if(StringUtils.hasText(userCode)){
                model.addAttribute("requestURI", "/oauth2/device_verification");
            }else{
                model.addAttribute("requestURI", "/oauth2/authorize");
            }
        }
        return "consent";
    }

    /**
     * Scope 에 따라서 description 을 추가하는 메서드
     */
    private static Set<ScopeWithDescription> withDescription(Set<String> scopes){

        Set<ScopeWithDescription> scopeWithDescriptions = new HashSet<>();

        for(String scope : scopes){
            scopeWithDescriptions.add(new ScopeWithDescription(scope));

        }
        return scopeWithDescriptions;
    }

    /**
     * 동의 화면 문구 출력
     */
    public static class ScopeWithDescription{

        private static final String DEFAULT_DESCRIPTION = "UNKNOWN SCOPE - We cannot provide information about this permission, use caution when granting this.";
        private static final Map<String, String> scopeDescriptions = new HashMap<>();

        static {
            scopeDescriptions.put(
                    OidcScopes.PROFILE,
                    "This application will be able to read your profile information."
            );
            scopeDescriptions.put(
                    "message.read",
                    "This application will be able to read your message."
            );
            scopeDescriptions.put(
                    "message.write",
                    "This application will be able to add new messages. It will also be able to edit and delete existing messages."
            );
            scopeDescriptions.put(
                    "other.scope",
                    "This is another scope example of a scope description."
            );
        }

        public final String scope;
        public final String description;

        ScopeWithDescription(String scope){
            this.scope = scope;

            // 매핑된 스코프를 꺼낸다 없으면 DEFAULT_DESCRIPTION 을 꺼낸다.
            this.description = scopeDescriptions.getOrDefault(scope, DEFAULT_DESCRIPTION);
        }

    }

}
