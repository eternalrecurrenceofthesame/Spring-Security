# 인증 구현

인증 논리를 담당하는 AuthenticationProvider 에 대해 알아보자! 

```
인증 프로세스는 요청하는 엔티티가 인증되지 않거나 요청하는 엔티티가 인증되는 두 가지 프로세스가 있다.

401: 애플리케이션이 사용자를 인식하지 못해 권한 부여 프로세스에 위임하지 않고 요청을 거부한다.
```

## AuthenticationProvider 의 이해 

### 인증 프로세스 중 요청 나타내기
```
공급자는 Authenticaiton 을 이용해서 인증을 검증한다. Authentication 인증 인터페이스는 
인증 요청 이벤트를 나타내며 애플리케이션에 접근을 요청한 엔티티의 세부 정보를 담는다. 

Authentication 은 인증 프로세스 도중과 이후에 이용할 수 있다. 

Authentication 인증 인터페이스는 애플리케이션에 접근을 요청하는 사용자를 주체(Principal) 를 확장한 것이다. 
```
```
* Authentication 의 구성
 
getName() // 인증하려는 사용자의 이름 Principal

getCredentials() // 사용자의 암호,코드,지문 등등 
getAuthorities() // 인증 후 사용자의 이용 권리 스프링 시큐리티에서 이용 권리는 권한을 나타낸다.
getDetails() // 시스템 요청에 대한 추가 세부 정보 제공
isAuthenticated() // 인증됐거나 인증 프로세스가 진행중을 나타낸다.
```

### 맞춤형 인증 논리 메서드 설명 

공급자가 인증 논리를 정의하려면 Authenticaiton 을 인자로 가지고 있는 authenticate(), supports() 메서드를 구현해야 한다.

```
* authenticatie() 메서드 구현 방법

인증에 실패하면 메서드는 AuthenticationException 을 던진다.

인증 공급자 구현에서 지원되지 않는 인증 객체를 받으면 null 을 반환한다. 
(이렇게 하면 HTTP 필터 수준에서 분리된 여러 Authentication 형식을 사용할 가능성이 생긴다. 9 장에서 설명)

인증에 성공하면 인증된 객체를 나타내는 Authentication 인스턴스를 반환한다 반환할 때는 
isAuthneticated() 메서드를 true 로 반환하며 인증 엔티티의 필수 세부정보가 포함된다. 

또한 애플리케이션은 인증에 사용된 민감 정보를 제거해야한다. 인증 후에는 비밀번호 같은 민감정보가 필요 없다.
```
```
* supports(Class<?> authentication) 메서드 구현 방법

인증 관리자는 인증 공급자를 호출할 때 supports 메서드를 호출해서 인증 공급자가 인증을 검증할 수 있는지
체크한다.

쉽게 말해서 이중으로 출입문을 세우는 것임 공급자가 인증 논리를 검증하기 전 관리자는 인증을 지원하는지 
확인하고 공급자에게 인증을 위임하는 것이다.

공급자가 Authentication 객체로 제공된 형식을 지원하면 true 를 반환. 공급자가 인증을 지원하더라도 
authenticate() 메서드에 null 값을 반환해서 요청을 거부할 수도 있다.

정리하자면 supports 메서드는 

Authentication 객체로 제공된 형식이 아니라 공급자에서 인증 요청을 할 수 없는 경우, 
Authentication 객체로 제공된 형식이지만 null 값을 반환해서 요청을 거부하는 경우,
Authentication 객체로 제공된 형식이고 인증 논리를 통해 인증에 성공하는 경우 3 가지가 있다. 119p
```

### AuthenticationProvider 구현하기 

CustomAuthenticationProvider 참고

```
* supports() 메서드 재정의

 @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType.equals(UsernamePasswordAuthenticationToken.class);
    }

인증 관리자에서 넘어온 Authentication 을 공급자가 지원 하는지 확인하는 메서드

인증 필터 수준에서 아무것도 맞춤 구성을 하지 않는다면 (9 장에서 다루는 내용) 120p
Authenticaiton 인터페이스의 구현체 중 하나인 UsernamePasswordAuthenticationToken 로 정의한다.
```
```
* authenticate() 메서드 재정의

ex5 CustomAuthenticationProvider 참고

인증에 성공하면 AuthenticationProvider 는 요청 세부정보를 포함하는 Authentication 을 '인증됨'으로
표시하고 반환한다.
```
```
* CustomAuthenticationProvider 등록하기

기존의 Websecurityconfigureradapter 가 deprecated 되었기 때문에 다른 방법을 이용해서 공급자를 등록해야 한다.
https://www.baeldung.com/spring-security-authentication-provider 참고

ex5 CustomAuthenticationProvider, CustomAuthenticationManager 참고 양방향 참조 조심! 
```

## SecurityContext 이용

인증 프로세스가 끝난 후 인증된 엔티티의 세부 정보가 필요할 수 있다. 인증된 사용자의 이름이나 권한을

다른 애플리케이션 로직에서 참조해야 되는 경우

인증이 성공한 후 인증 필터는 인증된 엔티티의 세부 정보를 보안 컨텍스트에 저장한다. 컨트롤러는 필요할 때

세부 정보에 접근할 수 있다.

### 보안 컨텍스트를 이용한 보유 전략 이용 MODE_THREDLOCAL
```
각 스레드가 보안 컨텍스트에 각자의 세부 정보를 저장하는 전략. 스프링 시큐리티가 보안 컨텍스트를 관리하는
*기본 전략* 으로써 스레드 로컬로 운영된다.

스레드 로컬이란 해당 스레드만 접근할 수 있는 특별한 저장소를 의미한 (스레드별 락커룸이라고 생각하자)
스프링 애플리케이션은 실행될 때 컴포넌트 스캔된 빈들이 각각 싱글톤 빈으로 구성되는데 여러 스레드에서 빈의 필드 값에

접근하면 스레드가 필드의 값을 공유하게 된다. 하지만 스레드 로컬을 사용하면 각 스레드별 요청 별로 정보를 저장할 수 있다.
보안 컨텍스트에 스레드 요청별 락커룸이 지급된다고 생각하면 된다.

이 전략은 보안 컨텍스트를 관리하는 기본적략으로써 이 프로세스는 명시적으로 구성할 필요가 없다.
```
```
* 컨트롤러에서 시큐리티 컨텍스트에 접근하는 방법

SecurityContext context = SecurityContextHolder.getContext(); 
Authentication a = context.getAuthentication();

a.getName 

지역 변수 안에 로직을 만들거나 

 @GetMapping("/hello")
    public String hello(Authentication a){
        return "Hello, " + a.getName() + "!";
    }
    
스프링 부트가 Authentication 을 메서드 인자에 주입해주는 것을 사용할 수 있다.

ex5 HelloController 참고 
```

### 비동기 호출을 위한 보유 전략 이용 MODE_INHERITABLETHREDLOCAL ?? 실습 부족 
```
대부분 앞서 설명한 기본 전략을 사용하지만 엔드포인트를 리액티브 스트림, 비동기로 만들면 요청의 각 단계별 
메서드를 실행하는 스레드와 요청을 수행하는 스레드가 병렬로 실행되기 때문에 서로 다른 스레드가 될 수 있다. 128p 

예를들면 /hello 엔드포인트에서 요청 엔티티의 세부 정보를 호출하려면 보안 인증을 받아야 하는데 보안 인증을 하고
리다이렉트로 /hello 를 호출하면 새로운 스레드가 되기 때문에 보안 컨텍스트의 스레드 로컬에 접근할 수 없게 된다.

@EnableAsync(클래스 레벨) + @Async(메서드 레벨) = 메서드가 별도의 스레드에서 실행된다.
```
```
MODE_INHERITABLETHREDLOCAL 전략을 활성화 하면 프레임워크는 요청의 원래 스레드에 있는 세부 정보를 비동기 메서드의 
새로 생성된 또는 병렬 스레드로 복사하게 된다.

ex5 mode_inheritablethredlocal config, controller 참고 

참고로 이 방식은 프레임워크가 자체적으로 스레드를 만들 때만 동작한다!! 130p 참고

직접 스레드를 만들거나 스레드 풀에서 스레드를 가져와서 사용하면 프레임워크가 이 스레드를 모르기 때문에
값이 제대로 복사되지 않음..
```

### 독립형 애플리케이션을 위한 보유 전략 이용 MODE_GLOBAL

보안 컨텍스트가 애플리케이션의 모든 스레드에서 공유되는 전략을 사용하고 싶을 때 MODE_GLOBAL 설정을 하면 된다.

일반적인 프로덕션 환경에서는 사용하지 않고, 독립형 애플리케이션의 경우 공유하는 것이 좋은 전략이 될 수 있다?? 131p

한마디로 스레드 로컬을 사용하지 않는다는 의미임
```
@Bean
public InitializingBean initiallizingBean(){
return () -> SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
}
```

## 보안 컨텍스트를 새로 생성한 스레드로 전파해보기
```
앞서 3 가지 모드를 이용해서 보안 컨텍스트를 관리하는 방법을 배웠다. 

기본적으로 스레드 로컬을 이용한 컨텍스트를 제공하며 비동기 요청이나 스레드가 새로 생성될 경우에는 보안 컨텍스트
전략으로 MODE_INHERITABLETHREDLOCAL 을 사용해야 한다. 

MODE_INHERITABLETHREDLOCAL 전략을 사용할 수 없는 경우에는 개발자가 직접 관리를 해줘야 한다. 
이것을 **자체 관리 스레드**라고 하여 개발자가 보안 컨텍스트 전파를 직접 해결해야 한다. 132p

한마디로 컨텍스트 값을 직접 새로운 스레드로 복사한다는 의미임.
```


### DelegatingSecurityContextRunnable 로 보안 컨텍스트 전달하기 
```
* DelegatingController 

DelegatingSecurityContextRunnalbe : 별도의 반환값이 없다
DelegatingSecurityContextCallable  : 반환값이 있을 때 사용

// 별도의 스레드에서 실행하고 싶은 Callble 을 장식할 때 사용하는 메서드들

간단하게 설명하자면 보안 컨텍스트를 복사하고 새로운 스레드를 가져온다. 
컨텍스트를 데코레이터 한 후  새로운 스레드에 데코레이팅된 보안 컨텍스트 인자를 넘겨주면 된다.

ex5 delegating @GetMapping("/ciao") 참고 
```

### DelegatingSecurityContextExecutorService 로 보안 컨텍스트 전달하기 
```
DelegatingSecurityContextExecutorService 를 사용하면 

앞서 살펴본 메서드와 달리 작업(Callable<String> task) 을 데코레이터 (new DelegatingSecurityContextCallable<>(task);) 
하지 않는 대신 특정 유형의 Executor 를 이용할 수 있다! 

ex5 delegating @GetMapping("/hola") 참고 
```
```
* 보안 컨텍스트에 대한 동시성 지원과 관련된 클래스들 136p 참고

필요에 맞게 사용하면 된다!

DelegatingSecurity-ContextExecutor
DelegatingSecurityContext-ScheduledExecutorService: 스케줄 된 스레드풀을 사용하는 데코레이터 
DelegatingSecurityContext-Runnalbe: 러너블 인터페이스를 구현하고 다른 스레드에서 실행되며 응답을 반환하지 않는 작업을 나타낸다.

그외 두 개는 앞서 만든 것들! 
```

## HTTP Basic 인증과 HTML 양식 기반 로그인 인증 이해하기

### HTTP Basic 이용 및 구성
```
기본적인 HTTP Baisc 을 사용하는 것이 충분할 수도 있지만, 인증 프로세스에 실패할 때를 위한 특정 논리를 구현하거나
클라이언트로 반환되는 응답값의 일부를 설정해야 할 수도 있다. 
```
```
* 인증 실패를 위한 응답의 영역 이름을 구성하기

ex 5 httpbasic config 참고 
```
```
http.httpBasic(c -> {
  c.realmName("OTHER");
  c.authenticationEntryPoint(new CustomEntryPoint());
});

AuthenticationEntryPoint 를 구현해서 인증에 실패했을 때를 위한 맞춤 구성을 만들고 민감한 데이터를 클라이언트에
노출하지 않도록 필터링하는 논리를 작성할 수 있다.

ex 5 httpbasic config CustomEntryPoint, ProjectConfig 참고
```

### HTML 양식 기반 로그인으로 인증 구현
```
양식 기반의 로그인으로 인증을 구현하는 것은 작은 애플리케이션에서 활용할 수 있다. 대형 애플리케이션은 보안 컨텍스트 관리에
서버 쪽 세션을 이용하지 않고 OAuth 2 를 사용한다 (12 ~ 15 장의 내용)

ex5 html_auth 참고 
```
```
* handlers 참고

핸들러는 세부적인 맞춤 구성을 위해 성공, 실패 두 가지 시나리오를 구현한다.

CustomAuthenticationSuccessHandler 는 로그인에 성공하고 권한에 일치할 때 home 으로 리다이렉션 한다.
권한이 없으면 다른 논리를 수행한다.

CustomAuthenticationFailureHandler 는 로그인에 실패했을 때 호출된다. 보통 로그인에 실패할 경우 
애플리케이션이 헤더값으로 요청 식별자를 보내고, 민감한 데이터를 시스템 외부로 유출하지 않도록 응답을 소독해야 한다. 145p

또한 로그인 실패 상황에 맞는 맞춤형 논리를 정의할 수도 있다. 

https://www.baeldung.com/spring-security-custom-authentication-failure-handler 
https://u2ful.tistory.com/35 참고 
```
```
핸들러를 만들고 시큐리티 필터 체인으로 핸들러를 주입 받으면 쉽게 사용 가능! html_auth ProjectConfig 참고

http.authorizeRequests()
   .anyRequest().hasRole("ADMIN")
   .and()
   .formLogin()
   .successHandler(authenticationSuccessHandler)
   .failureHandler(authenticationFailureHandler)
   .and().httpBasic();

return http.build();
```

