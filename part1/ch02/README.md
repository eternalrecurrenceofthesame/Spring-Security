# 안녕! 스프링 시큐리티

## 첫 번째 프로젝트 시작
```
HTTP basic 인증 후 REST 엔드 포인트에 접근하는 방법들을 알아본다. 참고로 HTTP basic 인증은 자격 증명을 요청에 포함시켜야 하기 때문에 
자격 증명의 기밀성을 보장하지 못한다.

Base64 는 단지 전송의 편의를 위한 인코딩 방법이고 암호화나 해싱 방법이 아니므로 전송중에 자격 증명을 가로채면 누구든지 볼 수 있다. 
HTTPS 와 함께 이용할 때가 아니면 HTTP Basic 인증은 사용하지 않는다. 43p
```
```
* 인터넷 클라이언트 사용
UserDetailsService 에 등록된 기본 아이디 + 비밀번호 (user + uuid) 로 인증 할 수 있다. 

* cURL 사용
curl -u user:9asdfsdaf... http://localhost:8080/hello // HTTP basic 인증 후 엔드포인트에 접근하는 커멘드 (cURL 사용)

* Base64 로 인코딩된 값 사용하기(cURL)
curl -H "Authorization: Basic Base64 인코딩 값" http://localhost:8080/hello

PasswordEncoder 에서 암호를 인코딩해서 암호가 기존 인코딩과 일치하는지 확인한다.
echo -n user:UUID 값(콘솔 uuid 값) | base64 // 리눅스나 Git Bash 콘솔을 이용해서 인코딩 할 수 있다!
```
```
Tip 인증 실패(401) 와 권한 부여 거부(403) 을 구분해서 사용해야 한다.

HTTP 401 - 해당 리소스에 대한 인증(사용자 식별)이 필요
HTTP 403 - 서버가 요청을 이해했지만 승인(권한 부여)을 거부
```
## 기본 구성이란?

아키텍처에서 인증과 권한 부여를 처리하는 데 참여하는 주요 구성요소에 대해 알아보자. 

(사전 구성된 요소를 애플리케이션의 필요에 맞게 재정의해서 사용해야 한다.)

```
* UserDetailsService (사용자 세부 정보 서비스)

애플리케이션의 내부 메모리에 기본 자격 증명을 등록하는 일을 한다. 스프링 부트가 제공하는 기본 값으로 
사용자 이름 'user' 기본 암호 'UUID'(콘솔에 보여줌) 를 사용할 수 있다. 
```
```
* PasswordEncoder (암호 인코더)

암호를 인코딩하고 암호가 기존 인코딩과 일치하는지 확인한다. UserDetailService 와 함께 존재한다(같은 생명주기?) 
직접 구현한다면 둘 다 같이 구현해야 한다.
```
```
* AuthenticationProvider(인증 공급자)

인증 공급자는 인증 논리를 구현한다. AuthenticationManager(인증 관리자) 에서 요청을 받고 사용자를 찾는 작업을 UserDetailsService 에, 
암호를 검증하는 작업을 PasswordEncoder 에 위임한다.
```
```
* 스프링 시큐리티 아키텍처의 주 구성 요소와 관계

엔드포인트 접근 요청이 들어오면 인증 필터에서 요청을 가로채고 보안 컨텍스트를 구성, 인증 관리자를 호출한다 인증 관리자는 파사드 같은 역할을
수행하는듯?? 사용자와 암호의 관리를 인증 공급자에게 위임하기 전 

별도의 로직을 수행?? 인증 공급자는 인코더와 사용자 세부 정보 서비스를 이용해서 **인증** 논리를 구현한다. 보안 컨텍스트는 인증된 엔티티에 관한
세부 정보를 저장!
```
#### + HTTP 와 HTTPS 비교, 리팩토링 예정 44P

실제 애플리케이션 통신은 HTTPS 를 사용해야 한다. HTTPS 를 구현하고 활성화 하는 방법에 대해 알아보자

(설명할 스프링 시큐리티 관련 구성은 HTTP 를 이용하든 HTTPS 를 이용하든 다르지 않음)

리팩토링 예정.

## 2.3 기본 구성 재정의 

지금까지 알아본 기본 값을 재정의 해보자! 
```
* UserDetailsService 재정의

UserDetailService 에 대해 알아보기 위해 서비스의 간단한 구현체인 InMemoryUserDetailsManager 를 구현해보자 
(예제나 개념 증명용으로 사용함 운영 단계에서는 사용하지 않음)

참고로 WebSecurityConfigurerAdapter 는 더 이상 사용되지 않는다!
https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
```
```
ProjectConfig 참고

@Configuration
public class ProjectConfig{

@Bean // 사용자 세부 정보 서비스
public inMemoryUserDetailsManager userDetailsService(){
  UserDetails user = User.withDefaultPasswordEncoder()
                         .username("user")
                         .password("12345")
                         .roles("user")
                         .build();
    return new InMemoryUserDetailsManager(user);                     
}

사용자 세부 정보를 메모리에 올려서 제공. 예제나 개념 증명용으로 사용됨 구현에서 사용하지 않는다.


@Bean
public PasswordEncoder passwordEncoder(){
  return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}

NoOpPasswordEncoder 인스턴스는 암호에 암호화나 해시를 적용하지 않고 일반 텍스트처럼 처리한다.
(String 클래스의 기본 equals 메서드로 간단한 문자열 비교만한다. 운영에서 사용 x)

값을 평문으로 저장하지 않게 다른 방법을 사용 https://chanho0912.tistory.com/33

@Bean
public SecurityFilterChain filterChain(HttpSecurity http){
  http.authorizeRequests(auth) -> auth.anyRequest().authenticated())
  .httpBasic(withDefaults());

return http.build
}

기존 방식이 아닌 필터 체인을 이용해서 모든 접근에 인증을 적용.

}
```
```
* AuthenticationProivder(인증 공급자) 구현 재정의

인증 공급자로 맞춤 구성 인증 논리를 구현해보자!
```
```
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        // user ,12345 의 요청만 승인된다. 
        //UserDetailsService, PasswordEncoder 를 대체
        if("user".equals(username) && "12345".equals(password)){ 
            return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList());
        }else{
            throw new AuthenticationCredentialsNotFoundException("오류!");
        }
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}

뒤에서 더 자세히 설명함.
```

* 프로젝트에 여러 구성 클래스 이용

사용자 및 암호를 관리하는 구성 클래스와 권한 부여를 위한 구성 클래스를 분리해서

응집성, 유지보수성을 높이자! (하나의 클래스가 하나의 책임을 맡도록 하자)

ex 패키지 참고.


