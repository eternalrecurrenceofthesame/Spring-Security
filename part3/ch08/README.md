# 권한 부여 구성: 제한 적용
```
특정한 요청 그룹(엔드포인트) 에만 권한 부여 제약 조건을 적용해보자.
일부 엔드포인트는 특정 사용자만 호출할 수 있고 나머지 엔드포인트는 모든 사용자가 호출할 수 있게 하기! 
```
```
권한 부여 구성을 적용할 때는 3 가지 메서드를 사용할 수 있다.

MVC 선택기: 경로에 MVC 식을 이용해 엔드 포인트를 선택한다.
앤트 선택기: 경로에 앤트 식을 이용해 엔드 포인트를 선택한다.
정규식 선택기: 경로에 정규식(regex) 을 이용해 엔드 포인트를 선택한다.
```

## 선택기 메서드 이해하기

3 가지 선택기에 대해 배우기 전 선택기 메서드를 이용하는 방법을 알아보자! 

```
* ex08 basic config, controller 참고 

관리자(admin) 는 /hello 엔드포인트를 호출할 수 있게 하고, 
운영자(manager) 는 /ciao 엔드 포인트를 호출할 수 있게 권한 부여 하기

// 유저 상세 정보
var user1 = User.withUsername("john")
                .password("12345")
                .roles("ADMIN") // 역할 부여
                .build();

// 필터 체인 (권한 부여 포맷 )
http.httpBasic();

http.authorizeRequests()
     .mvcMatchers("/hello").hasRole("ADMIN") // 관리자 역할
     .mvcMatchers("/ciao").hasRole("MANAGER")
     .anyRequest().permitAll(); // 명시적으로 지정해주기

권한 부여 규칙은 명시적으로 지정하는 것이 좋다. /hola 엔드포인트에 누구나 접근 할수 있다는 요구사항이 있으면
권한 부여 외의 어떤 요청이든 접근 가능하다는 것을 만들어 줘야 한다. 
```
```
* permitAll(), authenticated(), denyAll()

인증에 실패하면 401 응답이 생성되고 엔드포인트로 호출이 전달되지 않지만 접근할 수도 있다.

인증에 실패하더라도 permitAll() 을 명시적으로 지정한 엔드포인트에는 접근할 수 있다. 
인증에 실패했을 때 접근하지 못하게 하려면 authenticated() 나 denyAll() 을 명시적으로 지정해야 한다.
```
### MVC 선택기로 권한 부여 필터 설정하기
```
스프링 시큐리티는 기본적으로 CSRF 에 대한 보호를 적용하기 때문에 GET 요청 외의 다른 요청들은 제한된다. 199p

실습 진행을 위해 GET 외의 POST,PUT,DELETE 같은 엔드포인트를 노출할 수 있도록 http.csrf().disable(); 로 설정한다.
CSRF 를 비활성하는 것은 좋은 관행이 아니다. CSRF 보호는 ch 10 에서 다룬다 200p
```
```
* MVC 선택기를 이용한 권한 부여 필터 만들기 ex08 mvc, controller 참고

http.authorizeRequests()
    .mvcMatchers(HttpMethod.GET, "/a")
    .authenticated()
    .mvcMatchers(HttpMethod.POST, "/a")
    .permitAll()
    .anyRequest()
    .denyAll();

.mvcMatchers(HttpMethod.GET, "/a") // 기본적인 패턴

.mvcMatchers("/a/b/**") 
            ("/a/**/c")

            ("/a/*/c") 

** 연산자를 이용한 패턴은 ** 수에 제한이 없다.
/a/**/c == /a/b/d/c , /a/b/c/d/e/c  패턴 내에 제한없이 올 수 있다

* 연산자를 이용한 패턴은 * 에 어느것이나 올 수 있지만 하나만 올 수 있다.
/a/*/c == /a/b/c , /a/d/c  어느 것이나 가능은 한데 패턴 하나만 추가 가능 
```
```
* 특정 숫자만 허용하게 권한 부여 필터를 구성하기

@GetMapping("/product/{code}") // Get 으로 어떤 인자든 받는 엔드포인트
public String productCode(@PathVariable String code){return code;}

// 권한 부여 필터 
http.authorizeRequests()
.mvcMatchers("/product/{code:^[0-9]*$}") // 길이 관계 없이 숫자만 나타내는 정규식
.permitAll()
.anyRequest().denyAll();

/product/{param:regex} 매개변수의 값이 정규식과 일치할 때만 권한이 부여된다.

주의!! 교재에서는 mvcMatchers 를 사용했지만 실제로는 regex 가 적용되지 않음
regex 는 regexMatchers 를 사용하자.

```
### 앤트 선택기로 권한 부여 필터 설정하기 

앤트 선택기와 MVC 선택기의 차이점을 알고 사용하자! 보통 MVC 선택기가 권장된다.

```
MVC 선택기로 권한 부여 필터를 구성하면 요청에 슬래시를 추가한 경로까지 필터로 검증해준다.
ex) mvcMatchers("/hello") 이렇게만 설정해도 뒤에 슬래시 하나를 추가한 /hello/ 까지 필터로 검증한다.

** 이것은 아주 중요한 특징이다!! ** 
앤트 선택기를 사용하면 의도치 않게 경로를 보호되지 않는 상태로 방치할 수 있다. 209p
```
```
* ch08 ant 참고

http.httpBasic();
http.authorizeRequests()
       .antMatchers("/hello").authenticated(); 
       
/hello/ 경로는 보호 안 됨 !! MVC 선택기를 사용하자

antMatchers(HttpMethod method, String patterns)
antMatchers(String patterns)
antMatchers(HttpMethod method)  "/**" 와 같은 의미로 경로 관계 없이 특정 HTTP 방식 지정
```

참고로 스프링 시큐리티 7.0 버전부터는 SecurityFilterChain 구성이 람다식만 사용하도록 전면 개정되면서 위에서 설명한 많은 메서드들이

사라졌다!! (오히려 더 간단해짐) 관련 내용은 아래 링크를 참고한다.

https://docs.spring.io/spring-security/reference/migration-7/configuration.html

### 정규식 선택기로 권한 부여 필터 설정하기
```
정규식 참고 글

https://www.regular-expressions.info/tutorial.html 참고
https://chrisjune-13837.medium.com/정규식-튜토리얼-예제를-통한-cheatsheet-번역-61c3099cdca8

정규식 테스트 사이트
https://regexr.com
```
```
MVC 와 앤트 식으로해결할 수 없는 경우가 있다 예를들면 경로에 특정한 기호나 문자가 있으면 모든 요청을 거부한다!
이러한 시나리오에서는 정규식과 같은 더 강력한 식이 필요하다. 앞서 한번 사용해봄 

정규식은 간단하지만 간단한 시나리오에 적용하더라도 읽기 어렵다는 단점이 있다. 
MVC,앤트를 우선적으로 사용하고 대안이 없는 경우 정규식을 사용하자.

regexMatchers(HttpMethod method, String regex) // HTTP 방식별 제한 가능
regexMatchers(String regex) // 경로만 따짐 
```
```
* Regex 를 이용한 예시들 ex08 regex 참고 

이메일 주소만 권한을 부여 하고 싶을 때 

// 컨트롤러
@GetMapping("/email/{email}")
public String video(@PathVariable String email) {
return "Allowed for email " + email; } 

// 권한 부여 필터 regexMatchers 사용
 http.authorizeRequests()
                .regexMatchers("*(.+@.\\.com)")
                .permitAll()
                .anyRequest()
                .denyAll();

localhost:8080/email/jaime@exmaple.com
```
```
* 더 복잡한 예제

정규식 선택기는 많은 경로 패턴과 여러 경로 변수의 값을 참조할 수도 있다.

@GetMapping("/viedo/{country}/{language}")

http.authorizeRequests()
      .regexMatchers(".*/(us|uk|ca)+/(en|fr).*")
      .hasRole("PREMIUM")
      .anyRequest()
      .authenticated();
         
역할이 PREMIUM 인 사용자만 regex 정규식에 접근할 수 있고 다른 요청은 인증된 사용자가 접근할 수 있는 로직
정규식 선택기를 이용해서 두 개의 경로 변수 값을 받았다.
```
```
정규식은 경로에 대한 어떤 요구사항이라도 지정할 수 있는 강력한 툴이다. 하지만 정규식은 읽기 어렵고 상당히 
길어질 수 있으므로 마지막 수단으로 남겨두는 것이 좋다!

MVC, 앤트식으로 문제를 해결할 수 없을 때 사용하자.
```
