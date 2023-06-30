# OAuth 2 : 권한 부여 서버 구현하기

권한 부여 서버는 OAuth 2 아키텍처에서 작동하는 구성요소 중 하나로써 사용자를 인증하고 **클라이언트** 에 토큰을 제공하는 역할을 한다.

클라이언트는 리소스 서버가 노출하는 리소스에 접근하기 위해 이 토큰을 사용한다.
```
# Notice 

스프링 시큐리티를 이용한 권한 부여 서버 개발이 OAuth 프로젝트가 끝남에 따라 중단 됐고 한다. (mng.bz/9lm, 357p)
스프링 시큐리티 팀은 새로운 권한 부여 서버를 개발중이다 (mng.bz/4Be5)

스프링 시큐리티 위키를 통해서 시큐리티 프로젝트에서 구현되는 다양한 기능에 대한 뉴스를 확인할 수 있다. (mng.bz/Qx01)

커스텀 권한 부여 서버 대신 Keyclocak 이나 Okat 같은 툴을 선택할 수 있지만 이러한 선택지가 거부될 수도 있기 때문에
스프링 시큐리티가 제공하는 권한부여 서버 샘플을 직접 구현해본다.

Spring Authorization Server Reference:
https://docs.spring.io/spring-authorization-server/docs/current/reference/html/

Spring Security Reference:
https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/index.html
-> 여기서  리소스 서버를 만드는 방법을 확인할 수 있다. 

참고 자료
https://www.appsdeveloperblog.com/spring-authorization-server-tutorial/
https://www.baeldung.com/spring-security-oauth-auth-server

참고로 스프링 클라우드를 이용하는 권한부여 서버 구현방식은 지원이 중단됐다. 아래 링크 참고 
https://stackoverflow.com/questions/71081479/what-is-the-difference-between-spring-boot-starter-oauth2-client-spring-cloud-s
```
#### + 스프링 시큐리티 인가 서버 샘플 

스프링 스큐리티에서 인가 서버 샘플을 추가했다. https://github.com/spring-projects/spring-authorization-server/tree/main/samples 참고

인가 서버 샘플을 클론 코딩하면서 필요한 설명을 애플리케이션에 기술한다. authorization-server-sample 참고 

## 샘플 권한 부여 서버 만들기
```
OAuth 2 에서 가장 중요한 구성 요소는 액세스 토큰을 발행하는 권한 부여 서버다. 스프링 시큐리티에서 제공하는 권한 부여 서버 샘플로
OAuth 2 Authorization server 를 직접 구현한다.

그랜트 유형에 따라서 구현 순서 및 컴포넌트의 간단한 설명을 기술하며 구체적인 구현 구조 및 컴포넌트는 애플리케이션 구현 파일과 시나리오
내용을 참고한다.

아파치 라이이선스란 ?
https://namu.wiki/w/%EC%95%84%ED%8C%8C%EC%B9%98%20%EB%9D%BC%EC%9D%B4%EC%84%A0%EC%8A%A4 참고 
```
### 1. 승인 코드 그랜트유형

#### config
```
권한 부여서버는 Authentication(인증), Authorization(인가) 두 과정으로 나뉜다. 인증 설정은 formLogin 과 OAuth2Login 을 사용하고
인증에 성공하면 커스텀 토큰을 만들어서 세션 값으로 클라이언트 애플리케이션에 전달한다.

토큰을 관리하기 위한 OAuth2AuthorizationService 를 주입 받아서 사용할 수 있다.
토큰과 클라이언트 유형은 저장은 메모리 또는 데이터베이스에 JPA 를 사용해서 저장할 수 있다.

토큰 및 클라이언트 유형이 저장되는 데이터베이스 스키마 및 JPA 구현 내용은 jpa 패키지를 참고한다.
```
```
* application.yml

권한부여 서버에서 사용할 포트 및 외부 소셜 로그인 정보(구글, GIT HUB) 를 구성한다. 

OAuth 2 프레임워크의 메커니즘을 적용하기 위해 인가 서버, 클라이언트 서버, 리소스 서버 3 가지로 분리되며
각각 다른 포트를 가지게 된다.

데이터베이스를 사용해서 토큰을 관리할 경우 데이터베이스 설정도 등록한다. 
```
#### jwt jwk 생성하기  
```
* jose

jose 패키지에는 서명키와 키 생성 유틸 클래스가 있다. 키생성에 대해서는 구체적으로 설명할 부분이 없다. 샘플의 구현을 그대로 사용했다. 
```
#### federation
```
FederatedIdentityAuthenticationSuccessHandler
- 외부 소셜 로그인에 성공하면 이 핸들러가 동작하고 세션에 필요한 값을 저장한다.

FederatedIdentityIdTokenCustomizer
- 커스텀 토큰을 생성한다.

UserRepositoryOAuth2UserHandler
- OAuth2 로그인 사용자 정보를 저장하는 클래스 
```
### 승인 코드 그랜트유형을 처리하는 인가 서버의 전체적인 흐름 
```
권한이 없는 브라우저(사용자) 가 클라이언트 애플리케이션에 접근하면 클라이언트 애플리케이션은 브라우저를 권한 부여 서버의
로그인 페이지로 리다이렉트 시킨다.

클라이언트 애플리케이션은 브라우저(사용자) 를 인증 페이지로 리다이렉트 시킬 때 클라이언트 유형 정보
(client_id, redirect_uri, scope, state) 와 승인 코드를 요청하는 쿼리를 포함해서 리다이렉트 시킨다.
 (사용자 브라우저는 권한 부여 서버 인증 요청시 이 요청을 같이 보낸다.)

사용자는 로그인 페이지에서 formLogin 또는 OAuth2Login 방식으로 권한 부여 서버에 인증 요청을 한다. 

이때 토큰이 만들어지는 단계는 두 단계로 나뉘는데 처음 로그인에 성공하면 인증 토큰(Authneticaiton) 이 만들어지고
권한 부여 서버는 세션 값을 저장한 후 클라이언트에게 응답한다.

그리고 권한 부여 서버는 로그인한 사용자 계정의 권한 범위에 클라이언트가 접근하게 할 수 있는지 사용자에게 동의를
묻는 화면을 보여주는데 (개별 설정을 한 경우)

클라이언트에게 scope 를 제공하는 것을 동의하면 권한부여 서버는 브라우저에 클라이언트가 액세스 토큰을 요청할 수 있는
승인 코드를 전달한다. 

권한 부여 서버에 사용자가 인증 하는 과정에서 승인 코드 요청과 함께 클라이언트 유형 정보를 포함한다. 인증에 성공하면
승인 코드는 클라이언트 애플리케이션에 전달된다.

클라이언트는 승인 코드를 사용해서 액세스 토큰을 요청할 수 있는데 이 과정을 Authorization 이라고 한다.
권한 부여 서버는 액세스 토큰 (Authentication) 값을 세션에 저장하고
(액세스 토큰은 메모리 또는 JPA 를 이용해서 데이터베이스에 저정할 수 있다.) 

클라이언트 애플리케이션(브라우저) 에 액세스 토큰을 넘겨준다. 클라이언트 애플리케이션은 액세스 토큰을 사용해서 사용자의
리소스에 접근할 수 있게된다.

이때 클라이언트 애플리케이션은 단순히 액세스 토큰을 세션 값으로 넘겨주는 것 만으로는 사용자 리소스에 접근할 수는 없다.
클라이언트는 사용자 리소스에  접근할 수 있는 리소스 API 를 가지고 있다. 

리소스 API 를 호출하면서 클라이언트는 OAuth2AuthorizedClient 값을 포함한 요청을 하게 되는데 이 클래스는 클라이언트
애플리케이션이 액세스 토큰에 접근할 수 있게 해준다.

이 클라이언트 인스턴스는 ServletOAuth2AuthorizedClientExchangeFilterFunction 필터를 통해서 관리할 수 있다.
```
### 2. 디바이스 코드 그랜트 유형
```
디바이스 그랜트유형 추가 설정 

web.authentication.DeviceClientAuthenticationConverter 를 참고한다.
```
### 디바이스 코드 그랜트유형을 처리하는 인가 서버의 전체적인 흐름 
```
디바이스 그랜트 유형은 승인 코드 그랜트 유형과는 다른 부분이 있다. 승인 코드 그랜트 유형은 브라우저가 시작점이 된다.
사용자가 브라우저에서 인증에 성공하면 권한 부여 서버의 승인 코드가 클라이언트 애플리케이션에 전달되는 구조라면 

디바이스(애플 티비같은 것을 의미함) 유형은 디바이스(클라이언트) 에서 권한 부여 서버에 액세스 토큰을 요청하면서 흐름이 시작된다. 

디바이스 클라이언트는 액세스 토큰을 요청하면서 인가 논리에 필요한 사용자 코드와 사용자 인가 페이지 url, 승인 코드 값을 직접 받는다.
그리고 디바이스 클라이언트는 권한 부여 서버에 액세스 토큰 요청을 반복적으로 시도하는데

사용자 코드와 사용자 인가 페이지 url 로 사용자가 별도의 브라우저에서 인가에 성공하면 액세스 토큰을 얻을 수 있다.

디바이스 클라이언트가 API 를 호출하는 과정은 승인 코드 그랜트 유형과 마찬가지로 OAuth2AuthorizedClient 값을 포함한
요청을 하게된다.

이때 승인 코드 그랜트 유형과는 다르게 ServletOAuth2AuthorizedClientExchangeFilterFunction 를 거쳐서 provider 를
통해 필요한 추가적인 작업이 발생하게 된다 (스프링 시큐리티를 사용하는 경우)
```

## 기타 참고할 내용들 
```
* 권한 부여 서버 설정에 관해서

RegistredClientRepository - 클라이언트 세부 정보를 등록 (메모리 또는 데이터베이스에 저장 가능)
clientId, clientSecret - 클라이언트 아이디와 비밀을 등록한다. 클라이언트가 권한 부여 서버에 요청을 인증할 때 사용된다.

clientAuthenticationMethod - BASIC 방식을 사용하면 클라이언트 아이디와 시크릿이 concatenated 한 싱글 스트링 방식으로 인코딩되어 사용된다. 

POST 방식을 사용하면 HTTP BODY 에 값을 넣어서 전송해야 한다. 일반적으로 BASIC 을 사용하는듯? 
If the ClientId and the Client Secret values are submitted in the body of the HTTP Post request, 
then use CLIENT_SECRET_POST as the client authentication method.

authorizationGrantType - 인증 그랜트 유형을 설정한다. 참고로 OAuth 2.1 버전은 2.0 보다 더 적은 그랜트 유형을 지원한다.
Password grant, Implicit grant 그랜트 유형을 완전하게 지원하지 않는다.

The Oauth 2.1 version supports fewer authorization grant types than Oauth version 2.0. 
For example, the Password grant and the Implicit grant are omitted in Oauth version 2.1.
So the implementation of these grant types might not be fully supported.

redirectUri - 인증 성공시 리다이렉트 할 Uri 를 지정한다. 리다이렉트에 사용되는 8080 포트는 OAuth 인증 서버
실행중에 사용할 수 있는 포트다. // 앞서 서버 포트를 따로 만들어준 이유인듯?

.redirectUri("http://127.0.0.1:8080/authorized")
.redirectUri("http://127.0.0.1:8080/login/oauth2/code/users-client-odic")

The two redirect URIs that I configure above participate in the redirect-base authorization flow.
Once the user successfully authenticates with authorization server, the authorization code will be attached to 
http://127.0.0.1:8080/authorized. The 8080 is the port number on which the OAuth Client application is running.
If your client application is a Spring Boot Web application, then port number 8080 is the port number 
on which your Client application is running.
The users-client-oidc is a name of the client. We will configure it a bit later when working with the web client application.

scope - 클라이언트 권한 범위를 지정한다.
```
```
* IssuerURL (발행자 주소 세팅하기)

@Bean
public ProviderSettings providerSettings() {
    return ProviderSettings.builder()
            .issuer("http://auth-server:8000")
            .build();
            
localhost 로 권한 부여 서버(발행자)를 호출하게 되면 제대로 작동하지 않기 때문에 빈으로 발행자 주소를 등록하고 
127.0.0.1 auth-server 주소로 작동할 수 있도록 etc/hosts 파일에 발행자를 알려준다.

샘플에서는 이 설정을 사용하지 않는다. 
```
```
* KeyPairGenerator 를 이용해서 public/private 키 만드기 

권한 부여 서버는 JWT(액세스 토큰)을 발행할 때 Private 키를 사용해서 토큰에 서명한다.

토큰에 서명하는 이유는 토큰을 가로채서 누군가 변경하지 않았는지 확인하기 위함이다.
(서명을 이용해 내용이 변경되지 않았는지 확인할 수 있음 284 p)

구체적인 구현은 jose 패키지를 참고한다.
```
```
* authorizeHttpRequests 와 authorizeRequests 의 차이점 

The difference between authorizeRequests and authorizeHttpRequests is explained here.
The authorizeHttpRequests uses the new simplified AuthorizationManager API and the AuthorizationFilter,
while authorizeRequests uses the AccessDecisionManager and FilterSecurityInterceptor.
The latter will be deprecated in future version of Spring Security.

참고
https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-requests.html#servlet-authorization-filtersecurityinterceptor
https://stackoverflow.com/questions/73089730/authorizerequests-vs-authorizehttprequestscustomizerauthorizehttprequestsc
```
```
* 인가 정보를 JPA 를 사용해서 저장하는 방법

https://docs.spring.io/spring-authorization-server/docs/current/reference/html/guides/how-to-jpa.html 참고
```
