# 권한 부여 구성: 액세스 제한
```
권한 부여(Authorization) 는 식별(로그인)된 클라이언트가 요청된 리소스에 액세스할 권한이 있는지 시스템이 결정하는 프로세스다.
인증 필터를 통해 식별(로그인 인증)이 완료되면 사용자 세부 정보가 보안 컨텍스트에 저장되고, 요청이 권한 부여 필터로 위임된다. 171p

권한 부여 필터에서 요청을 허용할지 결정하고, 권한이 허용되면 요청 컨트롤러로 전달된다.

식별 == 인증 == 로그인 같은 의미! 

인증 필터 // 권한 부여 필터 는 서로 다른것! 
```

## 사용자 권한을 기준으로 엔드포인트에 접근 제한
```
애플리케이션의 권한 부여 필터는 인증된 사용자의 세부 정보 GrantedAuthority 컬렉션의 권한에 따라 권한 부여를 수행한다.

권한을 기준으로 접근을 제한하지 않으면 사용자가 애플리케이션의 모든 엔드포인트를 호출할 수 있다.
권한은 사용자가 수행할 수 있는 작업을 의미한다.
```
```
* 권한 지정 메서드들 ex7 ex1 config 참고

// 유저 상세 서비스
 var user1 = User.withUsername("john")
                .password("12345")
                .authorities("READ") // 권한 authorities 사용 
                .build();
                
// 시큐리티 필터 체인
http.httpBasic();

        http.authorizeRequests()
                .anyRequest()
                .hasAuthority("WRITE"); // 엔드포인트에 접근하기 위한 권한 지정.

.hasAuthority("WRITE"); // 필요한 권한을 지정
.hasAnyAuthority("READ","WRITE") // or 조건을 지정  

대부분 이 두 메서드로 제약 조건을 만들 수 있고 그렇게 하는 게 좋다. 이 옵션들로 해결이 되지 않고 좀 더 범용적인 
권한 부여 규칙을 구현하려면 access() 메서드를 이용하면 된다.

참고로 권한은 대문자 또는 소문자로 지정할 수 있다. 어떤 것을 사용하더라도 애플리케이션에서 일관성을 유지한다면
상관없다.
```
```
* access() 메서드 이용하기 

http.httpBasic();

        http.authorizeRequests()
                .anyRequest()
                .access("hasAuthority('read') and !hasAuthority('delete')"); 

access 는 SpEL 식으로 표현할 수 있다 권한이 read 인 클라이언트와 delete 가 아닌 클라이언트만 접근 가능
access 를 사용하면 복잡한 권한 설정이 가능해진다.
```

## 사용자 역할을 기준으로 모든 엔드포인트에 대한 접근을 제한

역할이란 사용자가 수행할 수 있는 작업을 나타낼 수 있는, 권한과 다른 방법이다. 실제 애플리케이션에서는 권한과 역할이 

같이 사용되므로 서로의 차이점을 이해하는 것이 중요하다.

```
권한: 제한이 적용되는 이용 권리 결이 고운(fine grained)
역할: 사용자는 해당 역할에 허가된 작업만 할 수 있다 결이 굵다(coarse grained)

쉽게 말해서 권한은 못하는 것들을 제한하는 거고, 역할은 할 수 있는 것을 지정하는 거임 
애플리케이션에서 역할을 이용하면 권한은 정의할 필요가 없다. 

권한은 개념상으로 존재하게 되고, 구현 요구 사항에 권한이 나올 수 있지만 사용자가 이용 권리를 가진 
*하나 이상의 작업*을 포함하는 역할만 정의하면 된다.
```
```
* ROLE 을 사용해서 역할 정의하기

역할을 정의할 때는 접두사 ROLE 을 사용한다.

hasRole(): 하나의 역할을 매개 변수로 받는다.
hasAnyRole(): 여러 개의 역할 이름을 매개 변수로 받는다.
access(): hasRole(), hasAnyRole() 을 SpEL 식으로 표현할 수 있다.

ex7 ex1 config 참고 

// 유저 상세 서비스
var user1 = User.withUsername("john")
                .password("12345")
                .authorities("ROLE_ADMIN") // 접두사 사용 
                .build();
                
// 필터 체인
 http.httpBasic();

        http.authorizeRequests()
                .anyRequest()
                .hasRole("ADMIN"); // 하나의 역할 지정 
```
```
* roles() 메서드를 사용해서 역할 지정하기

// 유저 상세 서비스
 var user1 = User.withUsername("john")
                .password("12345")
                .roles("ADMIN")
                .build();
               
유저 상세 서비스에 roles 를 사용하면 접두사를 생략할 수 있다.
```
### access() 메서드에 관한 추가 사항 
```
access() 는 권한과 역할을 넘어서 범용적 사용할 수 있다.

http.httpBasic();

http.authorizeRequests()
       .anyRequest().access("T(java.time.LocalTime).now().isAfter(T(java.time.LocalTime).of(12, 0))");

정오 이후부터 엔드 포인트에 접근을 허용하게 만들기.

SpEL 식 참고 docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions

SpEL 로 access() 를 만들면 어떤 규칙이라도 구현할 수 있다. 다만 애플리케이션에서 사용할 때는
hasRole(), hasAnyRole() 을 우선순위로 두고 이러한 옵션들로 해결할 수 없는 상황에만 사용한다. 
```

### 모든 엔드포인트에 대한 접근을 제한하기
```
// 시큐리티 필터 체인 
http.httpBasic();

        http.authorizeRequests()
                .anyRequest().denyAll();

denyAll() 메서드를 사용해서 모든 사용자의 접근을 제한할 수 있다.

마이크로 서비스에서 게이트웨이를 여러 개 사용할 때 각각의 게이트웨이가 지원하지 않는 엔드포인트에
접근 요청하는 것을 막을 때 사용할 수 있다. 190p 참고 

```

