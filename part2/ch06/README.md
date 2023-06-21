# 실전: 작고 안전한 웹 애플리케이션

지금까지 배운 내용으로 간단한 프로젝트를 만들어보자!

## 프로젝트 요구 사항과 설정

사용자가 인증에 성공하면 주 페이지에서 제품 목록을 볼 수 있는 작은 웹 애플리케이션 150p

```
* 스프링 부트로 SQL 스크립트 만들기

스프링은 SQL 스크립트를 사용해서 데이터베이스를 구성하는 것을 권장하지 않는다. schema.sql 은 작동하지만
data.sql 스크립트로 데이터를 넣는 것은 스프링 부트 3.1 기준 제대로 작동되지 않는다.

data.sql, schema.sql 참고 
https://wildeveloperetrain.tistory.com/228 참고 

실제 애플리케이션에서는 SQL 스크립트에 버전을 지정하는 솔루션을 이용할 수 있다. 
flywaydb.org, liquibase.org 와 같은 종속성을 이용하면도움이 된다. 
```
```
권한과 사용자 간에는 다대다 관계를 이용하는 것이 권장되지만, 이 예제에서는 시큐리티 측면에 집중하기 위해
사용자와 권한은 일대 다 관계로 매핑한다. 154 p 
```
```
* application.yml 설정하기 

datasource 의 아이디와 비밀번호는 외부로 노출하지 않고 비밀 볼트를 사용해야 한다.

spring.datasource:
  url: jdbc:mysql://localhost:3306/ex06?serverTimezone=UTC&characterEncoding=UTF-8
  username: 
  password: 
  driver-class-name: com.mysql.cj.jdbc.Driver

spring.jpa:
  hibernate:
    ddl-auto: none
  properties:
    hibernate:
      show_sql: true
      format_sql: true
      use_sql_comments: true


logging.level:
  org.hibernate.sql: DEBUG # 로그 레벨을 디버그로 설정해서 SQL 문과 실제 값을 출력한다. 상용환경에서는 지양한다.
```
## 예제 구현하기

예제 구현 순서대로 설명 전체적인 구조와 설명은 애플리케이션을 참고한다.

### entity

### model
```
모델 패키지에는 UserDetails 계약을 사용자 정의로 구현한다. UserDetails 는 AuthenticationProvider 가 인증 논리에
사용하는 UserDetailsService 로 호출하는 사용자 정보가 된다. 

UserDetails 계약을 구현할 때는 데이터베이스에 저장되는 엔티티와 UserDetails 세부 정보를 분리해서 구현해야 두 가지
책임이 혼합되지 않고 유지 보수성을 높일 수 있다. 
```
### service
```
서비스 패키지에는 AuthenticationProvider 와 AuthenticationManager 를 구현한다. 
```

전체적인 흐름을 정리하면 사용자는 애플리케이션의 리소스에 접근하기위해 인증을 한다. 시큐리티 구성 클래스로 formLogin 방식을

사용하면 UsernamePasswordAuthenticatonFilter 가 request 를 가로채고 principal(username) 과 credentials(password) 로 

구성된 UsernamePasswordAuthenticationToken 로 반환한다.

Token 값은 인증 매니저를 거쳐 인증 공급자로 전달되고 인증 공급자는 사용자 상세 정보 서비스를 사용해서 인증 논리를 진행한다. 

인증에 성공한 Authentication 은 SprincSecurityContext 에 저장된다. 

