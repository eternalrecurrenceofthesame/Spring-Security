# OAuth 2 - LogIn

시큐리티에서 OAuth 2 프레임워크 로그인을 만들어보자 ! 

https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html 참고

## 로그인 구성 설정

### pom.xml
```
스프링 시큐리티, 스프링 시큐리티 클라이언트, 스프링 웹
```

### application.yml
```
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: 구글 OAuth 2 클라이언트 아이디
            client-secret: 구글 OAuth 2 클라이언트 비밀번호
            
아이디와 비밀번호를 노출하지 말자 ! 

구글 클라우드 OAuth 2 API 를 설정하고 localhost:8080 을 호출하면 구글 인증 화면이 나온다.
인증을 하게되면 클라이언트(웹 브라우저)는 필요한 정보를 세션에 저장한다. 

At this point, the OAuth Client retrieves your email address and basic profile information
from the UserInfo Endpoint and establishes an authenticated session.

인증후 리다이렉트 되면 클라이언트는 액세스 토큰을 요청할 수 있는 승인 코드를 가지게 된다. 
https://github.com/eternalrecurrenceofthesame/Spring-security-in-Action/tree/main/ch13 참고 
```

## CommonOAuth2Provider (인증 공급자)

스프링 시큐리티에서 보편적으로 사용할 수 있는 인증 공급자(CommonOauth2Provider) 로는 Google, GitHub, Facebook, and Okta 가 있다. 
```
* 권한 부여 서버(인증 공급자) 를 호출하는 흐름

필터 체인 OAuth2LoginAuthenticationFilter 은 권한 부여 서버(인증 공급자) 의 세부 정보를 얻을 수 있다. 346p
ClientRegistrationRepository 는 하나 이상의 CleintRegistartion(권한 부여 서버 - Google, Github, Facebook) 객체를 가질 수 있다 

로그인 클라이언트(브라우저) 는 OAuth2LoginAuthenticationFilter -> ClientRegistrationRepository 인증 필터에서 인증 흐름을 수행하기 위해 
저장소에서 인증 공급자의 세부 정보 (클라이언트 ID, 비밀, URL 범위 등) 를 가져다 인증 서버와 연결한다. 

CommonOauth2Provider 를 사용하면 authorization-uri, token-uri, and user-info-uri 값은 default 값으로 사용할 수 있고
사용자는 client id, secret 만 yml 으로 설정하면 된다.

For example, the authorization-uri, token-uri, and user-info-uri do not change often for a provider. 
Therefore, it makes sense to provide default values, to reduce the required configuration.
```
## Configuring Custom Provider Properties

### yml 로 인증 공급자 설정하기
```
* application.yml 참고
```
### 자바 자동-구성(autoconfiguration) 으로 인증 공급자 설정하기
```
* OAuth2LoginConfig 참고

보통 모든 구성 설정을 사용하진 않는다. 커스텀 인증 서버를 만든다면 참고하여 사용하자! 
```

## OAuth 2 로그인 클라이언트 시큐리티 설정하기

로그인을 위한 시큐리티 설정을 만들어보자! 앞서 만든 것은 OAuth 설정!

### Register a SecurityFilterChain @Bean

```
* OAuth2LoginSecurityConfig 참고

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
    .oauth2Login(Customizer.withDefaults()); // OAuth 2 Login Config 를 인자로 받음

return http.build();}
```

