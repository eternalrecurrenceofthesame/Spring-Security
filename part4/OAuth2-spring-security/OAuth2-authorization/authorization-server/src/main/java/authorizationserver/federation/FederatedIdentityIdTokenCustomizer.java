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
package authorizationserver.federation;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.*;

/**
 * An {@link OAuth2TokenCustomizer} to map claims from a federated identity to
 * the {@code id_token} produced by this authorization server.
 *
 * @author Steve Riesenberg
 * @since 1.1
 */

/**
 * 아이디 토큰 값을 수정하는 클래스
 */
public final class FederatedIdentityIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {
    private static final Set<String> ID_TOKEN_CLAIMS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            IdTokenClaimNames.ISS,
            IdTokenClaimNames.SUB,
            IdTokenClaimNames.AUD,
            IdTokenClaimNames.EXP,
            IdTokenClaimNames.IAT,
            IdTokenClaimNames.AUTH_TIME,
            IdTokenClaimNames.NONCE,
            IdTokenClaimNames.ACR,
            IdTokenClaimNames.AMR,
            IdTokenClaimNames.AZP,
            IdTokenClaimNames.AT_HASH,
            IdTokenClaimNames.C_HASH
    )));

    /**
     * OAuth 2 Token Context 의 Token Type 을 사용자 타입으로 수정하는 메서드
     *
     * 간단하게 설명하자면 사용자 정의 principal 토큰 값을 사용하기 위해서 기존의 토큰 저장소의 토큰 매핑 값을 모두 지우고
     * 사용자 정의 principal 을 저장하는 메서드이다.
     */
    @Override
    public void customize(JwtEncodingContext context) {

        if(OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())){ /** 아이디 토큰 값을 조건으로 설정  */

            Map<String, Object> thirdPartyClaims = extractClaims(context.getPrincipal()); /** 기존 토큰 저장소의 principal 값을 다 꺼낸다 */

            context.getClaims().claims(existingClaims -> { /** 아이디 토큰의 클레임 값을 꺼낸다 */

                // Remove conflicting claims set by this authorization server
                existingClaims.keySet().forEach(thirdPartyClaims::remove); /** 기존 아이디 토큰의 클레임 값을 꺼내고 커스텀 클레임과의 중복을 방지하기 위해 커스텀에서 제거해준다. */

                //Remove standard id_token claims that could cause problems with clients
                ID_TOKEN_CLAIMS.forEach(thirdPartyClaims::remove);

                // Add all other claims directly to id_token
                existingClaims.putAll(thirdPartyClaims); /** 사용자 claim 을 context 에 저장 */

            });
        }
    }

    /**
     * principal 값을 추출하는 메서드로 OIDC 와 일반 OAuth2User 를 구분해서 principal claim 값을 꺼낸다.
     */
    private Map<String, Object> extractClaims(Authentication principal){
        Map<String, Object> claims;
        if(principal.getPrincipal() instanceof OidcUser){
            OidcUser oidcUser = (OidcUser) principal.getPrincipal();
            OidcIdToken idToken = oidcUser.getIdToken();
            claims = idToken.getClaims();

        } else if(principal.getPrincipal() instanceof OAuth2User){
            OAuth2User oauth2User = (OAuth2User) principal.getPrincipal();
            claims = oauth2User.getAttributes();
        }else{
            claims = Collections.emptyMap();
        }
        return new HashMap<>(claims);
    }
}
