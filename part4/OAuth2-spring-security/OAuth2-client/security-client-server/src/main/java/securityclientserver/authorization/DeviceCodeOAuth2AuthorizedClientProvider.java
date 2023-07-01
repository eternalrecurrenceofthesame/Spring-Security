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
package securityclientserver.authorization;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Steve Riesenberg
 * @since 1.1
 */

/**
 * 디바이스 그랜트 유형에서 사용할 공급자를 구현한다. 디바이스 그랜트 유형인 경우에만 authorize 를 적용하며
 * 이 공급자는 WebClient 에서 리소스 API 를 호출할 때 적용된다.
 */
public class DeviceCodeOAuth2AuthorizedClientProvider implements OAuth2AuthorizedClientProvider {

    private OAuth2AccessTokenResponseClient<OAuth2DeviceGrantRequest> accessTokenResponseClient =
            new OAuth2DeviceAccessTokenResponseClient();

    private Duration clockSkew = Duration.ofSeconds(60);

    private Clock clock = Clock.systemUTC(); // System UTC

    public void setAccessTokenResponseClient(OAuth2AccessTokenResponseClient<OAuth2DeviceGrantRequest> accessTokenResponseClient){
        this.accessTokenResponseClient = accessTokenResponseClient;
    }

    public void setClockSkew(Duration clockSkew) {
        this.clockSkew = clockSkew;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }


    /**
     * 디바이스 그랜트 유형만을 따로 분리하는 이유는 브라우저가 아닌 클라이언트가 직접 API 를 호출하고 사용자 Authorization 을 기다려야 하기 떄문인 것 같다.
     */
    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {

        Assert.notNull(context, "context cannot be null"); // 디바이스 Authentication 체크

        ClientRegistration clientRegistration = context.getClientRegistration();
        if(!AuthorizationGrantType.DEVICE_CODE.equals(clientRegistration.getAuthorizationGrantType())){
            return null;
        }

        /**
         * 디바이스 토큰 만료시 새 토큰을 요청한다. 액세스 토큰이나 리프레시 토큰이 만료되지 않았다면 원래 값을 반환한다.
         */
        OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();
        if(authorizedClient != null && !hasTokenExpired(authorizedClient.getAccessToken())){
            // If client is already authorized but access token is NOT expired than no need for re-authorization
            return null;
        }

        if(authorizedClient != null && authorizedClient.getClientRegistration() != null){

            // If client is already authorized but access token is expired and a
            // refresh token is available, delegate to refresh_token.
            return null;
        }

        // *****************************************************************
        // Get device_code set via DefaultOAuth2AuthorizedClientManager#setContextAttributesMapper()
        // 디바이스 코드를 사용해서 액세스 토큰을 요창하고 토큰 값을 포함해서 OAuth2AuthorizedClient 를 반환한다.
        // *****************************************************************

        String deviceCode = context.getAttribute(OAuth2ParameterNames.DEVICE_CODE);
        // Attempt to authorize the client, which will repeatedly fail until the user grants authorization
        OAuth2DeviceGrantRequest deviceGrantRequest = new OAuth2DeviceGrantRequest(clientRegistration, deviceCode);
        OAuth2AccessTokenResponse tokenResponse = getTokenResponse(clientRegistration, deviceGrantRequest);

        return new OAuth2AuthorizedClient(clientRegistration, context.getPrincipal().getName(),
                tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

    }

    /**
     * 편의 메서드들
     */

    // 토큰 요청 메서드 액세스 토큰과 리프레쉬 토큰이 만료되었다면 이 요청을 호출하고 새 인가 과정을 거친다.
    private OAuth2AccessTokenResponse getTokenResponse(ClientRegistration clientRegistration,
                                                       OAuth2DeviceGrantRequest deviceGrantRequest){

        try{
            return this.accessTokenResponseClient.getTokenResponse(deviceGrantRequest);
        }catch(OAuth2AuthorizationException ex){
            throw new ClientAuthorizationException(ex.getError(), clientRegistration.getRegistrationId(), ex);
        }
    }

    // 시스템 UTC 만료 시간을 지난 경우 false 반환
    private boolean hasTokenExpired(OAuth2Token token){
        return this.clock.instant().isAfter(token.getExpiresAt().minus(this.clockSkew));
    }

    // 처음 디바이스 인증시 DEVICE_CODE 를 가져오는 매퍼
    public static Function<OAuth2AuthorizeRequest, Map<String, Object>> deviceCodeContextAttributesMapper(){
        return (authorizeRequest) -> {
            HttpServletRequest request = authorizeRequest.getAttribute(HttpServletRequest.class.getName());
            Assert.notNull(request, "request cannot be null");

            // Obtain device code from request
            String deviceCode = request.getParameter(OAuth2ParameterNames.DEVICE_CODE);
            return (deviceCode != null) ? Collections.singletonMap(OAuth2ParameterNames.DEVICE_CODE, deviceCode) :
                    Collections.emptyMap();

        };
    }
}
