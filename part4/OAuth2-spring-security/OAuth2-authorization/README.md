# OAuth 2 : 권한 부여 서버 구현하기

스프링 시큐리티에서 OAuth 2 프레임 워크의 구성 요소를 직접 구현 한다!

권한 부여 서버는 OAuth 2 아키텍처에서 작동하는 구성요소 중 하나로써 사용자를 인증하고 **클라이언트** 에 토큰을 제공하는 역할을 한다.

클라이언트는 리소스 서버가 노출하는 리소스에 접근하기 위해 이 토큰을 사용한다.

```
# Notice 

스프링 시큐리티를 이용한 권한 부여 서버 개발이 OAuth 프로젝트가 끝남에 따라 중단 됐고 한다. (mng.bz/9lm, 357p)
스프링 시큐리티 팀은 새로운 권한 부여 서버를 개발중이다 (mng.bz/4Be5)

스프링 시큐리티 위키를 통해서 시큐리티 프로젝트에서 구현되는 다양한 기능에 대한 뉴스를 확인할 수 있다. (mng.bz/Qx01)

스프링 시큐리티 권한 부여 서버가 어느 정도 성숙? 하려면 시간이 필요하며 이번 단원에서는 스프링 시큐리티를 이용해
맞춤형 권한 부여 서버를 개발할 수 있는 유일한 방법을 알아본다.

맟줌형 권한 부여 서버 대신 Keyclocak 이나 Okat 같은 툴을 선택할 수 있지만 이러한 선택지가 거부될 수도 있다.

이번 단원에서는 교재의 예제를 사용하지 않는다. 교재의 예제 방식으로 시큐리티 인증 서버를 만들 수도 있지만 
##spring-cloud-starter-oauth2## 을 사용하는 방식은 지원되지 않고 있다.

https://stackoverflow.com/questions/71081479/what-is-the-difference-between-spring-boot-starter-oauth2-client-spring-cloud-s

스프링 시큐리티는 인증서버를 구성하는 OAuth 2 를 사용하는 방법을 지원한다. 

Spring Authorization Server Reference:
https://docs.spring.io/spring-authorization-server/docs/current/reference/html/

Spring Security Reference:
https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/index.html
-> 여기서  리소스 서버를 만드는 방법을 확인할 수 있다. 

참고 자료
https://www.appsdeveloperblog.com/spring-authorization-server-tutorial/
https://www.baeldung.com/spring-security-oauth-auth-server
```
#### + 스프링 시큐리티 인가 서버 샘플 

스프링 스큐리티에서 인가 서버 샘플을 추가했다. https://github.com/spring-projects/spring-authorization-server/tree/main/samples 참고

인가 서버 샘플을 클론 코딩하면서 필요한 설명을 애플리케이션에 기술한다. authorization-server-sample 참고 

## 맞춤형 권한 부여 서버 만들기

OAuth 2 에서 가장 중요한 구성 요소는 액세스 토큰을 발행하는 권한 부여 서버다.  

### config 





### 권한 부여 서버 자바 설정 만들기 

AuthServerConfig 참고 (권한 부여 서버 설정)
```
먼저 RegistredClientRepository 를 빈으로 등록해야 한다. 이 구성 클래스는 권한 부여 서버에 클라이언트를 등하록고
클라이언트의 세부 정보를 설정하는 클래스이다.

클라이언트 등록은 메모리 또는 JDBC 를 이용해서 데이터베이스에 저장할 수 있다. 여기서는 예제 진행을 위해 메모리 저장소를 사용

* clientId, clientSecret: 클라이언트 아이디와 비밀을 등록한다. 클라이언트가 권한 부여 서버에 요청을 인증할 때 사용된다.

* clientAuthenticationMethod: 인증 메서드 방식을 지정한다 BASIC 방식을 사용하면 클라이언트 아이디와 시크릿이 concatenated 한 싱글
스트링 방식으로 인코딩되어 사용된다. 

POST 방식을 사용하면 HTTP BODY 에 값을 넣어서 전송해야 한다. 일반적으로 BASIC 을 사용하는듯? 
If the ClientId and the Client Secret values are submitted in the body of the HTTP Post request, 
then use CLIENT_SECRET_POST as the client authentication method.

* authorizationGrantType: 인증 그랜트 유형을 설정한다. 참고로 OAuth 2.1 버전은 2.0 보다 더 적은 그랜트 유형을 지원한다.
Password grant, Implicit grant 그랜트 유형을 완전하게 지원하지 않는다.

The Oauth 2.1 version supports fewer authorization grant types than Oauth version 2.0. 
For example, the Password grant and the Implicit grant are omitted in Oauth version 2.1.
So the implementation of these grant types might not be fully supported.

* redirectUri: 인증 성공시 리다이렉트 할 Uri 를 지정한다. 리다이렉트에 사용되는 8080 포트는 OAuth 인증 서버
실행중에 사용할 수 있는 포트다. // 앞서 서버 포트를 따로 만들어준 이유인듯?

.redirectUri("http://127.0.0.1:8080/authorized")
.redirectUri("http://127.0.0.1:8080/login/oauth2/code/users-client-odic")

The two redirect URIs that I configure above participate in the redirect-base authorization flow.
Once the user successfully authenticates with authorization server, the authorization code will be attached to 
http://127.0.0.1:8080/authorized. The 8080 is the port number on which the OAuth Client application is running.
If your client application is a Spring Boot Web application, then port number 8080 is the port number 
on which your Client application is running.
The users-client-oidc is a name of the client. We will configure it a bit later when working with the web client application.

* scope: 클라이언트 권한 범위를 지정한다.
```

### 권한 부여 서버 구성 설정에 필요한 빈 만들기

AuthServerConfig 참고
```
* To configure default security

시큐리티 필터 체인으로 OAuth 2 디폴트 로그인 페이지 설정 메서드 참고

* IssuerURL // 발행자 주소 세팅하기 

@Bean
public ProviderSettings providerSettings() {
    return ProviderSettings.builder()
            .issuer("http://auth-server:8000")
            .build();
            
localhost 로 권한 부여 서버(발행자)를 호출하게 되면 제대로 작동하지 않기 때문에 빈으로 발행자 주소를 등록하고 
127.0.0.1 auth-server 주소로 작동할 수 있도록 etc/hosts 파일에 발행자를 알려준다. (뒤에서 부연 설명)
```
```
* KeyPairGenerator 를 이용해서 public/private 키 만드기 

권한 부여 서버는 JWT(액세스 토큰)을 발행할 때 Private 키를 사용해서 토큰에 서명한다.

토큰에 서명하는 이유는 토큰을 가로채서 누군가 변경하지 않았는지 확인하기 위함이다.
(서명을 이용해 내용이 변경되지 않았는지 확인할 수 있음 284 p)

AuthServerConfig JWK 키 만드는 메서드 참고 
```

### 스프링 시큐리티 설정 만들기

간단하게 사용자와 폼 로그인 화면을 설정했다. WebSecurityConfig 참고 

```
@Bean
public SecurityFilterChain configureSecurityFilterChain(HttpSecurity http) throws Exception {
        
http.authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated())
.formLogin(Customizer.withDefaults());

 return http.build();

권한 부여 서버에 접근하고 사용자 인증을 하기 위해서는  authorizeHttpRequests 를 사용해야 한다 //Http 가 추가됨.

authorizeHttpRequests 와 authorizeRequests 의 차이점 

The difference between authorizeRequests and authorizeHttpRequests is explained here.
The authorizeHttpRequests uses the new simplified AuthorizationManager API and the AuthorizationFilter,
while authorizeRequests uses the AccessDecisionManager and FilterSecurityInterceptor.
The latter will be deprecated in future version of Spring Security.

참고
https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-requests.html#servlet-authorization-filtersecurityinterceptor
https://stackoverflow.com/questions/73089730/authorizerequests-vs-authorizehttprequestscustomizerauthorizehttprequestsc
```

## 권한 부여 서버 테스트해보기

```
* 승인 코드 요청

http://127.0.0.1:8000/oauth2/authorize?response_type=code&client_id=client&redirect_uri=http://127.0.0.1:8080/authorized&scope=openid read
사용자 인증 후 클라이언트에서 승인 코드를 얻을 수 있다.
```
```
* 포스트 맨으로 승인 코드를 이용해서 액세스 토큰 얻기 

POST http://127.0.0.1:8000/oauth2/token 
Authorizaiton 탭에서 클라이언트 아이디와 시크릿을 입력한다.
Body 의 x-www-form-urlencoded 탭에서 grant_type, code, redirect_uri 를 입력한다 

포스트를 요청하면 액세스 토큰을 얻을 수 있다! 

```


