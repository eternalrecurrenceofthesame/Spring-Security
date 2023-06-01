# 필터 구현

## 스프링 시큐리티 아키텍처의 필터 구현
```
인증 필터는 요청을 가로채고 인증 책임을 권한 부여 필터에 위임한다. 인증 이전에 특정 논리를 실행하고 싶다면 인증 필터 앞에 
필터를 추가하면 된다.

필터를 추가할 때 여러 필터가 같은 위치에 있으면 필터가 호출되는 순서는 정해지지 않는다. 224p
```

### 체인에서 인증 필터(기존 필터) 앞에 필터 추가하기
```
* Before 맞춤형 필터 시나리오 예제

모든 요청에 Request-Id 헤더가 있다고 가정, 애플리케이션은 이 헤더로 요청을 추적한다. 인증을 수행하기 전에 헤더가 있는지 검증하며,
요청의 형식이 유효하지 않다면 데이터베이스 쿼리나 다른 리소스를 소비하는 작업을 수행하지 않는다.

ex09 before(기존 필터 앞이라는 의미!) 참고 필터를 만들고, 필터체인으로 등록! 
```

### 체인에서 인증필터(기존 필터) 뒤에 필터 추가하기
```
* After 맞춤형 필터 시나리오 예제

기존 BasicAuthenticationFilter 뒤에 맞춤형 필터를 추가하자! 인증 필터 뒤에 성공한 인증 이벤트를 모두 기록하는 필터를 추가해보겠음.
인증 필터를 통과하면 인증 성공 이벤가 발생했다는 가정하에 기록한다.

ex09 after(기존 필터 뒤라는 의미!) 참고
```

### 필터 체인의 다른 필터 위치에 필터 추가하기
```
HTTP Basic 인증 흐름 대신 다른 인증을 구현해서 사용자 자격을 증명하기. 
(Basic 에서 사용하는 사용자 이름과 암호 대신 다른 접근법을 구현)
```
```
* 인증을 위한 정적 헤더 값에 기반을 둔 식별

서버에서 생성한 정적 키를 쿠키에 담아서 응답하면 사용자는 Http 요청시 쿠키에 담긴 헤더 값을 전송한다. 
서버에서는 데이터베이스나 비밀 볼트에 저장된 쿠키의 헤더값으로 사용자를 인식한다.

이 방식은 보안 수준은 낮지만 단순하다는 장점이 있다. 
보안 측면에서 인프라?? 수준에 의존하면서 엔드포인트를 완전한 비보호 상태로 두지 않는 절충안에 해당한다. 231p
```
```
* 대칭 키를 이용해 인증 요청 서명

대칭키란? 클라이언트와 서버가 모두 알고 있는 키를 의미한다. 클라이언트는 이 키로 요청의 일부에 서명하고 (특정 헤더값)
서버는 같은 키로 서명이 유효한지 확인한다. ch11 토이 프로젝트 참고

* 인증 프로세스에 OTP 이용 ch11 토이 프로젝트 참고

Google Authenticatior 와 같은 인증 공급자 앱으로 OTP 를 받는다.
```

### 맞춤형 필터 예제 만들어보기

모든 요청에 대해 헤더의 정적 키 값을 사용해서 클라이언트 인증하기, 클라이언트는 Authorizaition 헤더에

정적 키의 값을 추가해서 요청해야 인증 받을 수 있다.
```
* StaticKeyAuthenticationFilter ex09 static 참고 

StaticKeyAuthenticationFilter 는 Authorization 헤더 값으로 전달되는 키 값을 서버에서 데이터베이스에 저장된
키 값과 비교해서 인증을 확인한다. (요청시 헤더에 Authorization 키 값이 있어야 인증 가능)

예제에서는 편의를 위해 설정 파일에 키 값을 만들고 불러와서 사용했다.
authorization.key=SD9cICjl1e // application.properties 


http.basic() 을 호출하지 않으면 BasicAuthenticationFilter.class (기본 인증 필터) 가 추가되지 않는다.
예제로 만든 정적 키 필터를 이 자리에 넣고 인증 필터를 대체할 수 있다.

http.addFilterAt(filter, BasicAuthenticationFilter.class) // staticfilter config 참고 
```
```
Tip

체인의 같은 위치에 여러 필터를 추가하면 안 된다. 같은 위치에 필터를 추가하면 순서가 보장되지 않는다.
필터 체인에 필요 없는 필터는 아예 추가하지 않아야 한다. 224, 234p

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class}) 
이 설정을 하면 유저 상세 정보 자동 구성을 비활성화 할 수 있다. (기본 user, password 값이 생성되지 않음) 236p
```

### 스프링 시큐리티가 제공하는 필터 구현하기
```
프레임워크는 필터 체인에 추가한 필터를 요청당 한번만 실행하도록 보장하지는 않는다.

GenericFilterBean: web.xml 설명자 파일에 정의하여 초기화 매개 변수를 이용할 수 있다.
OncePerRequestFilter: doFilter 메서드가 요청당 한번만 실행된다.

ex09 after OneceAuthenticationLoggingFilter 참고 
```
```
* OncePerRequestFilter

shouldNotFilter 메서드를 재정의 해서 필터가 적용될지 결정하는 논리를 구현할 수 있다. 
기본적으로 필터는 모든 요청에 적용됨.

OncePerRequestFilter 는 비동기 요청이나 오류 발송 요청에는 적용되지 않는다. 

비동기 요청이나 오류 발송 요청을 사용하고 싶다면 ??
shouldNotFilterAsyncDispatch(), shouldNotFilterErrorDispatch() 메서드를 재정의하면 된다.
```
