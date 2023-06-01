# 리액티브 앱을 위한 스프링 시큐리티

## 리액티브 앱이란?
```
리액티브 프로그래밍은 작업 단계를 순차적으로 기술(명령형)하는 것이 아니라 데이터가 전달될 파이프라인을 
구성해야한다. 그리고 파이프라인을 통해 데이터가 전달되는 동안 데이터는 어떤 형태로든 변경 사용 될 수 있다. 

spring 5 ch 10 참고
```

### 리액티브 앱에서 사용되는 스프링 시큐리티 특징
```
명령형 애플리케이션에서는 스레드 로컬을 이용해서 시큐리티 컨텍스트에 접근할 수 있었다면, 리액티브 애플리케이션에서는
비동기 호출 전략을 이용하거나 보안 컨텍스트를 새로운 스레드로 직접 전파해줘야 한다.

시큐리티 컨텍스트의 사용이 달라졌기 때문에 모든 권한 부여 구성이 영향을 받게 된다. 엔드 포인트 계층에 적용된 보안과
전역 메서드 보안 기능이 영향을 받는다.

UserDetailsService 는 하나의 데이터 원본이므로 사용자 세부 정보 서비스도 리액티브 방식을 지원해야 한다?? 532 p
```

## 리액티브 앱에서 사용자 관리하기

인증 필터 -> ReactiveAuthenticationManaer -> (ReactiveUserDetailsService, PasswordEncoder) 리액티브 시큐리티는

명령형 시큐리티 프로세스보다 단순하다.

```
* 리액티브 타입 유저 상세 서비스 reactivesecurity ProjectConfig 참고 

@Bean
public userDetailsService(){
   var u = User.withUsername("john")
                .password("12345")
                .authorities("read")
                .build();
var uds = new MapReactiveUserDetailsService(u);
return uds;}

ReactiveUserDetailsService 를 사용한다.
```
```
* 리액티브 반환 타입

@GetMapping("/hello")
public Mono<String> hello(Mono<Authentication> auth){
   Mono<String> message = auth.map(a -> "Hello " + a.getName());

  return message;}

리액티브 타입으로는 단일 데이터를 가지는 Mono, 복수의 데이터를 가지는 Flux 타입이 있다
```
```
* ReactiveSecurityContextHolder

앞서 설명했듯이 리액터는 비동기 호출을 하기 때문에 보안 컨텍스트를 스레드 로컬로 운영할 수 없다. 스프링 시큐리티는
ReactiveSecurityContextHolder 를 제공해서 비동기 애플리케이션에서 SecurityContext 작업을 할 수 있게 지원한다. 537 p

@GetMapping("/helloreactive")
public Mono<String> hello(){
     Mono<String> message = ReactiveSecurityContextHolder.getContext() 
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> "Hello " + auth.getName());

return message;}

ReactiveSecurityContextHolder 에서 Mono<SecurityContext> 값을 호출하면 context 에서 비동기로 Authentication 객체에 접근할 수 있다.
.map 메서드는 동기화를 적용해서 방출된 항목에 접근할 수 있게 해준다. 

map(): Transform the item emitted by this Mono by applying a synchronous function to it
```

## 리액티브 앱에서 권한 부여 규칙 구성하기

애플리케이션에서 인증된 사용자는 보안 컨텍스트에 저장된 후 권한 부여 규칙이 적용된다.

### 리액티브 애플리케이션의 엔드 포인트 계층에 권한 부여 적용하기
```
리액티브 애플리케이션에서 필터 체인을 생성할 때는 SecurityWebFilterChain 를 구현한다. 

@Bean
public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
return http.authorizeExchange()
      .pathMatchers(HttpMethod.GET, "/hello")
      .authenticated()
      .anyExchange()
      .permitAll().and().httpBasic().and().build();}

리액티브 방식에서는 두 구성 요소 간의 의사 소통을 보통 exchanging data 라고 한다. 
```
### access() 를 사용해서 복잡한 권한 구성 만들어보기 (리액티브)

access() 메서드를 사용하면 복잡한 권한 구성을 만들 수 있지만 애플리케이션의 가독성이 떨어진다는 단점이 있다.

hasRole(), hasAuthority() 를 우선순위에 두고 필요한 경우 사용한다. ch 7 
```
AuthorizationDecision 은 애플리케이션에 요청이 허용되는지 알려주는 역할을 한다. 
access 로 getAuthorizationDecisionMono 을 호출해서 요청이 허용되는지 관련 메서드 로직을 돌리고 결괏값을 반환한다.

ex 19 reactivesecurity ProjectConfig 참고 546 p 
```
## 리액티브 앱에 메서드 보안 적용하기

리액티브 메서드 보안은 권한 부여 애노테이션(@PreAuthorize, @PostAuthorize) 애노테이션만 이용할 수 있고

사전,사후 부여 필터는 아직 구현되지 않았다. 

github.com/spring-projects/spring-security/issues/5249 참고

### 메서드 보안 구현하기
```
@EnableReactiveMethodSecurity 구성 설정 클래스에 리액티브 메서드 보안을 활성화 한다. reactivesecurity ProjectConfig 참고

crul -u john:12345 http://localhost:8080/hello 
```

## 리액티브 앱과 OAuth 2

리액티브 시큐리티 리소스 서버를 만들어보자! 

https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html 참고
