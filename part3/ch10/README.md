# CSRF 보호와 CORS 적용

스프링 시큐리티에는 필터 체인에 추가하는 자체 필터도 있다.

## 애플리케이션에 CSRF(사이트 간 요청 위조) 보호 적용

지금까지 작성한 대부분의 예제는 HTTP GET 만으로 엔드포인트를 구현했다. HTTP POST 를 구현할 때는 CSRF(기본적으로 활성화됨)

보호를 비활성화 하는 보조 명령을 추가해야 했음.

CSRF 는 광범위한 공격이고 CSRF 에 취약한 애플리케이션은 사용자가 웹 애플리케이션에서 원치않는 작업을 실행하게 할 수 있다.

### 스프링 시큐리티의 CSRF 보호가 작동하는 방식

```
CSRF 공격은 사용자가 웹 애플리케이션(타깃)에 로그인했다고 가정한다. 사용자가 악의적인 코드가 들어 있는 외부 사이트를 이용하면
공격자는 애플리케이션(타깃) 에서 작업을 실행하는 악의적 스크립트(공격) 가 포함된 리소스를 받게된다. 

이 스크립트는 이미 인증된 사용자의 서버에 접근해서 사용자 대신 원치 않는 작업을 실행할 수 있다. 241p
```
```
* 어떻게 하면 사용자를 보호할 수 있을까?

CSRF 보호는 웹 애플리케이션에서 프로트엔드 작업(GET,HEAD,TRACE,OPTIONS 외의 HTTP 방식) 을 수행할 수 있게 보장하고
외부 페이지가 사용자 대신 변경 작업을 수행할 수 없게 한다.

데이터 변경 작업을 수행하려면 최소한 한번은 HTTP GET 요청을 수행해야 한다. 이때 애플리케이션은 고유한 토큰을 생성하고
HTTP 요청의 _csrf 특성에 추가해서 사용자에게 응답한다. 243p 

애플리케이션은 헤더에 고유한 토큰이 있는 요청에 대해서만 변경 작업(POST,PUT,DELETE)을 수행한다.

CSRF 보호의 시작점은 필터 체인의 CsrfFiler 이다. 이 필터는 요청을 가로채고 GET,HEAD,TRACE,OPTIONS 를 포함하는 HTTP 방식을
모두 허용하고 다른 모든 요청에는 CSRF 토큰이 포함되어 있는지 확인한다. (default 필터임)

헤더가 없거나 잘못된 토큰이 포함된 변경 요청이 있을 경우 403 응답을 보낸다.
```

## 실제 시나리오에서 CSRF 보호 사용하기 

CSRF 보호는 서버에서 생성된 리소스를 이용하는 페이지가 같은 서버에서 생성된 경우에만 이용한다!

프론트엔드와 백엔드를 모두 담당하는 단순한 아키텍처에서 잘 동작함. 

모바일 애플리케이션인 클라이언트가 있거나 프론트와 백엔드가 독립적으로 개발된 경우 다른 방법을 사용해야 한다! 253p

(11 ~ 15 장에서 설명) 

### CSRF 보호를 활성화하고 POST 호출하기 ex10 ex1 참고
```
CSRF 토큰은 GET 요청시 프레임워크가 생성해서 응답 값으로 넘겨준다.

토큰 사용시 개발자의 관점에서 CSRF 토큰을 사용하려면 CsrfFilter 요청 다음으로 토큰을 확인하는
로거를 만든 후 GET 요청에서 생성된 토큰 로그를 이용해서 POST 요청에 토큰을 사용하면된다

하지만 클라이언트 사용자는 서버에서 로그 값을 확인할 수 없다. 즉 클라이언트가 POST 요청을 할 수 있게
하려면 서버는 클라이언트가 사용할 HTTP 응답에 _csrf 토큰 값을 히든 필드로 추가해서 넘겨줘야 한다.
```
### csrfex2 참고 html 사용
```
애플리케이션 기본 로그인 페이지에 접근(GET)하면 서버에서 바디의 히든 필드로 _csrf 값을 보내는 것을 확인할 수 있다.
로그인 후 main.html 에서 _csrf 가 적용된 상태로 POST 요청을 하려면 _csrf 값을 사용해야 한다. 

POST 요청에서 _csrf 값을 사용하려면 서버가 보낸 _csrf 값을 HTML 바디에 히든 필드를 만들어서 타임리프로 값을 받고 
POST 요청을 하면 CSRF 필터가 요청을 가로채고 요청을 허용하면 POST 작업을 수행할 수 있다. 

Tip
CsrfFilter 는 토큰 값을 _csrf 특성에 추가한다.

TIP
데이터 변경 리소스를 만들때 HTTP GET 을 사용하지 않도록 해야한다. GET 은 CSRF  토큰 없이도 수행할 수 있다! 253p
```
### CSRF 보호 맞춤 구성 만들기 csrfex3 참고 restapi 사용
```
앞서 설명했지만 CSRF 보호는 서버에서 생성된 리소스를 이용하는 페이지가 같은 서버에서 생성된 경우에만 이용한다.
(일체형 애플리케이션을 의미한다.)

CSRF 보호는 GET,HEAD,TRACE,OPTIONS 외의 HTTP 방식으로 호출되는 엔드포인트의 모든 경로에 적용된다.
CSRF 를 일부 애플리케이션 경로에만 비활성화 하기! 

http.csrf(c -> {  // 람다 인자로 CsrfConfigurer 을 사용한다 
            c.ignoringAntMatchers("/ciao"); // POST
        });
        http.authorizeRequests()
                .anyRequest().permitAll();

좀 더 범용적인 방법을 사용하고 싶다면 RequestMatcher 을 이용하면 된다. csrfex3 config 참고
```
```
* CSRF 토큰 관리 방법을 커스텀 하기

작은 애플리케이션의 경우 서버 세션 저장소에 토큰을 저장할 수 있지만 요청 처리가 많아지고 수평적 확장이 필요하다면 
데이터베이스에 토큰을 저장하고 관리할 수 있다.

데이터베이스에 저장될 토큰을 만들고, 토큰 저장소를 만들어주면 된다.


CsrfToken - CSRF 토큰 자체를 기술한다.

스프링 시큐리티는 CSRF 토큰을 기술할 수 있는 DefaultCsrfToken 이라는 구현체를 제공한다.
토큰은 필드 값으로 토큰의 헤더(X-CSRF-TOKEN), 특성(_csrf), 토큰값(uuid) 를 가질 수 있고

커스텀 토큰을 구현하지 않으면 기본적으로 디폴트 값으로 설정된 이름을 가진다. (default) 

CsrfTokenRepository - CSRF 토큰을 생성, 저장, 로드하는 객체를 기술한다.
```

### 커스텀 리포지토리와 토큰을 사용해서 시나리오 만들기 csrfex3 참고
```
로그인에 성공하면 로그인 세션값을 받는다. 로그인 세션값이 있는 상태에서 다른 HTTP 요청을 하면 258p
CsrfFilter 는 CustomTokenRepository 를 사용해서 토큰을 생성하고 데이터베이스에 저장한다

로그인 세션 ID(Identifer):토큰(UUID)

로그인 세션값은 로그인 중 얻을 수있고, 로그인할 때 마다 달라야 한다.

로그인 세션의 대안으로 수명이 정의된 CSRF 토큰을 이용하는 방법이 있다. 이경우 특정 사용자 세션과
토큰을 연결하지 않고, 데이터베이스에 토큰만 저장한다.

요청을 허용할지 결정하려면 HTTP 요청을 통해 제공된 토큰이 존재하는지 만료되지 않았는지 확인하면 된다. 258 p 

예제에서는 따로 로그인 세션값은 만들지 않고 수 X-IDENTIFIER 헤더 값에 수동으로 값을 넘겨준다.

세션과 관련된 내용은 https://github.com/eternalrecurrenceofthesame/Spring/tree/main/session 을 참고한다.

CsrfFilter -> CustomRepository -> JpaTokenRepository -> MySQL //
```

## CORS(교차 출처 리소스 공유) 이용
```
브라우저는 사이트가 로드된 도메인 이외의 도메인에 대한 요청을 허용하지 않는다 example.com 에서 사이트를 열었다면 브라우저는
이 사이트에서 api.example.com 에 요청하는 것을 허용하지 않는다. 266 p 그림 참고 

예를 들면 example.com 이라는 웹 사이트에서 (프론트 엔드 애플리케이션을 가정) API 서버의 api.example.com 을 호출하려고 할 때
CORS 설정이 허용되어 있지 않다면 접근이 거부된다.

CORS 를 사용하면 애플리케이션이 요청을 허용할 도메인, 그리고 공유할 수 잇는 세부 정보를 지정할 수 있다. CORS 는 HTTP 헤더를
기반으로 작동한다.
```
```
* 주요 헤더

Access-Control-Allow-Origin : 도메인의 리소스에 접근할 수 있는 외부 도메인을 지정한다.
(로드된 도메인 이외의 도메인에 대한 요청을 지정한다는 의미)

Access-Control-Allow-Methods : 특정 http 방식만 허용하고 싶을 때 지정할 수 있다.
Access-Control-Allow-Headers : 특정 요청에 이용할 수 있는 헤더에 제한을 추가한다.
```
### CORS 실습 애플리케이션 구현
```
html 에서 자바 스크립트로 api 를 호출하는 시나리오를 구현한다. 이때 자바스크립트가 있는 html 에서 CORS 테스트를
위해 localhost 도메인을 사용하지 않고 127.0.0.1 IP 주소로 api 를 호출하게 한다. 

이렇게 하는 이유는 브라우저는 같은 호스트를 나타내더라도 문자열이 다르면 서로 다른 도메인으로 인식하기 때문에
CORS 를 요청을 테스트 할 수 있다.
```
```
* 실습 진행

리소스 애플리케이션에서 CORS 설정을 하지 않아도 브라우저에서 localhost:8080 을 호출하면 main.html 에서 로드된 도메인
이외의 127.0.0.1:8080/test 는 호출된다.

로드된 도메인(localhost) 이 아닌 127.0.0.1:8080/test 을 API 로 호출하면 엔드포인트는 호출되지만 실제 자바스크립트
호출에대한 오류가 콘솔에 찍히게되고 리소스 응답은 표시되지 않는다.

자바스크립트 오류가 발생하는 이유는 로드된 도메인 이외의 도메인 값으로 요청을 호출했기 때문이다. 오류 메시지에는
Access-Control-Allow-Origin HTTP 헤더가 없어서 응답이 수락되지 않았다고 나오는데

localhost:8080 도메인의 리소스에 접근할 수 있는 127.0.0.1:8080 외부 도메인 설정을 애플리케이션에서 하지 않았기 때문이다. 

이때 중요한 점은 브라우저에서 CORS 보호가 적용되더라도 자바스크립트에서 실제 API 엔드포인트를 호출하고 호출 됐다는 것이다.
즉 CORS 제한이 적용되더라도 일부 상황에서는 엔드포인트가 호출된다.

하지만 브라우저는 응답에 출처가 지정되지 않으면 응답을 수락하지 않는다. CORS 메커니즘은 결국 브라우저에 관한 것이며
엔드포인트는 보호되지 않는다. 
```
### @CrossOrigin 애노테인셔으로 CORS 정책 적용하기

로드된 도메인이 아닌 요청을 브라우저에서 수락하게 하기 

```
* 엔드포인트에 적용

    /**
     * 자바 스크립트에서 호출하는 API 를 가정한다.
     */
    @PostMapping("/test")
    @ResponseBody
    @CrossOrigin("http://localhost:8080") // 교차 출처 요청을 허용한다.
    public String test(){
        logger.info("Test API method Called");
        return "Hello";
    }


@CrossOrigin({"example.com", "example.org"}) 로 여러 개 지정할 수 있으며
allowedHeaders 특성과 methods 특성으로 허용되는 헤더와 메서드를 지정할 수도 있다.

출처와 헤더에 * 를 이용해서 모든 출처나 헤더를 지정할 수 도 있지만 이렇게 하면 
XSS 에 노출되고 DDos 공격에 취약해질 수 있다.
```
@CrossOrigin 으로 직접 규칙을 지정하면 규칙이 투명해지는 장점이 있지만 코드가 장황해지고  반복되는 코드가 많아지는 단점도 있다. 

```
* 자바 시큐리티 설정으로 cors 적용하기

    /**
     * 모든 요청을 허용한다. POST 를 API 요청으로 사용하기 위해 CSRF disable 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(c -> {
            CorsConfigurationSource source = request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(
                        List.of("example.com", "example.org"));
                config.setAllowedMethods(
                        List.of("GET", "POST", "PUT", "DELETE"));
                
                return config;
            };
        });
      
        http.csrf(c -> c.disable());

        http.authorizeHttpRequests(a ->
                a.anyRequest().permitAll());

        return http.build();
    }
```

전체적인 구성을 보고 싶다면 cors-ex 를 참고한다. 

