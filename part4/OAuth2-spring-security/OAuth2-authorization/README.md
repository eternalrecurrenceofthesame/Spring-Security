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

OAuth 2 에서 가장 중요한 구성 요소는 액세스 토큰을 발행하는 권한 부여 서버다. 시큐리티가 제공하는 샘플 서버를 만들고 간단한 설명을 기술한다. 

샘플은 OAuth 2 메커니즘을 사용한 일반적인 폼 방식의 로그인과  OAuth 2 를 이용한 소셜 로그인 방식을 구현한다. 전체적인 틀과 구체적인 설명은

예제 애플리케이션에 기술되어 있으며, 여기서는 권한부여 서버를 만들어가는 과정 및 전체적인 흐름을 개략적으로 기술한다. 

아파치 라이이선스란 ?

https://namu.wiki/w/%EC%95%84%ED%8C%8C%EC%B9%98%20%EB%9D%BC%EC%9D%B4%EC%84%A0%EC%8A%A4 참고 

### 승인 그랜트 유형 구현하기 
```
* config 

권한 부여서버는 Authentication(인증), Authorization(인가) 두 과정으로 나뉜다. 인증 설정은 formLogin 과 OAuth2Login 을 사용하고
인증에 성공하면 커스텀 토큰을 만들어서 세션 값으로 클라이언트에 전달한다.

토큰을 관리하기 위한 OAuth2AuthorizationService 를 주입 받아서 사용할 수 있다.
토큰과 클라이언트 유형은 저장은 메모리 또는 데이터베이스에 JPA 를 사용해서 저장할 수 있다. jpa 패키지 참고 
```


### config 
```
* config 참고 

권한부여 서버의 설정은 AuthorizationServer 설정과 Authentication 설정으로 나뉜다. 권한 부여 서버에서 인가 및 인증 정보에 대한 것을 담당한다. 
(권한 부여 서버에서 로그인 및 회원가입에 관한 것을 관리)


```
```
* application.yml

권한부여 서버에서 사용할 포트 및 외부 소셜 로그인 정보(구글, GIT HUB) 를 구성한다. 

OAuth 2 프레임워크의 메커니즘을 적용하기 위해 인가 서버, 클라이언트 서버, 리소스 서버 3 가지로 분리되며
각각 다른 포트를 가지게 된다. 
```
###  jose
```
jose 패키지에는 서명키와 키 생성 유틸 클래스가 있다. 키생성에 대해서는 구체적으로 설명할 부분이 없다.
샘플의 구현을 그대로 사용했다. 
```
### authentication

디바이스 그랜트 타입에서 사용할 인증 공급자와 인증 유형을 커스텀으로 구현한다.

```
* 디바이스 그랜트 타입이란?

웹 브라우저가 아닌 사물 인터넷 기기를 권한 부여 서버에서 인가할 때 사용하는 그랜트 유형으로 리소스 서버에 접근하는
디바이스의 인가를 웹 브라우저에서 진행하고 디바이스에 리소스 서버에 접근할 수 있는 토큰을 부여하는 방식 

디바이스 그랜트 타입 대략적인 설명  
https://pragmaticwebsecurity.com/img/articles/device-flow/deviceflow.png
```
### federation
```
인증 성공 핸들러 및 Authentication principal Customzier 클래스가 있다. 
```
### 인가 서버 구현의 전체적인 흐름 설명

인가되지 않은 클라이언트가 리소스에 접근하면 인가 서버에서 제공하는 로그인 페이지로 이동된다. 로그인 페이지에서 사용자 인증(폼, 소셜 로그인) 에

성공하면 인증 성공 핸들러가 호출된다. 인증 성공핸들러는 세션에?  Authentication principal 값을 저장한다.

인증 요청을 할 때 클라이언트 서버는 인가 서버에 승인 코드와 상태 값을 요청한다. 클라이언트 서버는 인가 서버가 제공하는 승인 코드와 상태값으로 

인가 서버에 액세스 토큰을 요청할수 있다.

액세스 토큰을 요청받은 인가 서버는 RegisteredClientRepository 에 등록된 클라이언트인지 확인하고 토큰을 생성한다. 

토큰을 생성할 때 인가 정보를 내장 데이터베이스에 저장할 수 있으며 인가 서버는 인가된 정보를 저장 및 삭제 조회할 수 있다. AuthorizationServerConfig 참고 

토큰을 받은 클라이언트는 토큰을 사용해서 리소스 서버에 접근할 수 있게 된다! 

```
* 인가 정보를 JPA 를 사용해서 저장하는 방법

https://docs.spring.io/spring-authorization-server/docs/current/reference/html/guides/how-to-jpa.html 참고
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
