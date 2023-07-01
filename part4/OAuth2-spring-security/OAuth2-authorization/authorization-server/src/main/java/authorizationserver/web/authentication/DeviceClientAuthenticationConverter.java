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
package authorizationserver.web.authentication;

import authorizationserver.authentication.DeviceClientAuthenticationToken;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

/**
 * @author Joe Grandja
 * @author Steve Riesenberg
 * @since 1.1
 */

/**
 * AuthenticationConverter 는 HttpServletRequest(액세스 토큰 요청) 를 특정한 Authentication 타입으로 변경할 때 사용된다.
 * 여기서는 디바이스 그랜트 요청 값을 DeviceClientAuthenticationToken 값으로 컨버팅 한다.
 *
 * 일반적인 흐름에서는 브라우저에서 사용자 Authentication 을 하고 Authorization 과정을 거친 후 Converter 가 작동한다.
 * (일반적인 승인 코드 그랜트 흐름을 생각하면 된다.)
 *
 * 디바이스 흐름은 브라우저가 없다 디바이스에서는 브라우저를 거치지 않고 디바이스에서 바로 토큰을 호출한다 토큰 호출 과정에서
 * 액세스 토큰을 얻기 위한 유저 코드와 url 주소값을 받고 디바이스 클라이언트는 사용자가 별도의 브라우저에서 Authorization 할
 * 때까지 디바이스 클라이언트는 반복적으로 액세스 토큰값에 대한 요청을 한다.
 *
 * 즉 일반적인 흐름과는 다르게 인증 -> 인가 과정이 순차적으로 진행되는 게 아니라 인증과 인가(poll)의 과정이 거의 동시에 발생하고
 * 클라이언트에서 액세스 토큰 값을 기다리는 구조이기 때문에 인증과 동시에 컨버터가 작동한다.
 *
 * 이 컨버터는 디바이스 그랜트 유형인 경우 DeviceClientAuthenticationToken 을 생성하고
 * DeviceClientAuthenticationProvider 를 호출한다. 그리고 데이터베이스나 메모리에 인증(인가) 값을 저장한다.
 */
public class DeviceClientAuthenticationConverter implements AuthenticationConverter {

   private final RequestMatcher deviceAuthorizationRequestMatcher;
   private final RequestMatcher deviceAccessTokenRequestMatcher;

   /**
    * Matcher 를 구현하고 디바이스 요청이 맞는지 매칭한다.
    */
   public DeviceClientAuthenticationConverter(String deviceAuthorizationEndpointUri){

      RequestMatcher clientIdParameterMatcher = request ->
              request.getParameter(OAuth2ParameterNames.CLIENT_ID) != null;

      this.deviceAuthorizationRequestMatcher = new AndRequestMatcher(
              new AntPathRequestMatcher(
                      deviceAuthorizationEndpointUri, HttpMethod.POST.name()),
              clientIdParameterMatcher);

      this.deviceAccessTokenRequestMatcher = request ->
              AuthorizationGrantType.DEVICE_CODE.getValue().equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)) &&
                      request.getParameter(OAuth2ParameterNames.DEVICE_CODE) != null &&
                      request.getParameter(OAuth2ParameterNames.CLIENT_ID) != null;
   }

   @Nullable
   @Override
   public Authentication convert(HttpServletRequest request) {
      if(!this.deviceAccessTokenRequestMatcher.matches(request) &&
              !this.deviceAccessTokenRequestMatcher.matches(request)){
         return null;
      }

      // client_id (REQUIRED)
      String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
      if(!StringUtils.hasText(clientId) || request.getParameterValues(OAuth2ParameterNames.CLIENT_ID).length != 1){
         throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
      }

      return new DeviceClientAuthenticationToken(clientId, ClientAuthenticationMethod.NONE, null, null);
   }
}
