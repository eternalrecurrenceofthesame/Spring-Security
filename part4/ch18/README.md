# 실전: OAuth 2 애플리케이션

OAuth 2 리소스 서버 안에서 전역 메서드 보안을 적용하는 실전 프로젝트를 만들어본다!
```
리소스 서버는 스프링 시큐리티를 이용해서 구현하며, 인증 서버는 Keycloak 오픈 소스를 사용한다. 실제 구현에 인증을 
적용할 때 Keycloak 오픈 소스를 사용하는 경우가 많다. 

또는 Okta, Auth0, LoginRadius 와 같은 Keycloak 의 대안 오픈 소스를 사용할 수도 있다.
```

## 애플리케이션 시나리오

사용자의 운동 기록을 저장하는 피트니스 애플리케이션을 만든다. 애플리케이션 구현은 애플리케이션이 운동 기록을 저장하는

부분에 중점을 둔다. 
```
* 백엔드 구현 기능 

사용자의 새 운동 기록 추가 
사용자의 모든 운동 기록 찾기
운동 기록 삭제 

각 작업은 기능 정의에 따라 특정 보안 제한이 적용되며, 역할은 관리자와 표준 사용자 두 개로 나뉜다. 

사용자는 자기 운동 기록을 추가하고 볼 수 있으며 관리자는 모든 사용자의 운동 레코드를 삭제할 수 있다. 
관리자도 사용자의 역할이 가능하다.

기능은 백엔드 OAuth 2 리소스 서버로 구현하고, 인증 서버는 Keycloak 을 사용한다. 
```
## Keycloak 을 권한 부여 서버로 구성하기

키 클록 인증 서버와 리소스서버를 만드는 세부적인 내용에 대해서는 아래의 링크를 참고한다 (스프링 부트 3.0)

https://medium.com/geekculture/using-keycloak-with-spring-boot-3-0-376fa9f60e0b

```
* 키 클록 시작 및 설정 

키 클록 다운로드 사이트 https://www.keycloak.org/downloads
다운로드 후 bin 폴더에서 cmd 를 이용해 kc.sh 파일을 실행한다 kc.sh start-dev 

서버 시작 후 locahost:8080 을 호출해서 서버에 접근할 수 있다. 첫 페이지에서 사용자 이름과 암호를 입력해 
관리자 계정을 구성한다. 생성한 관리자 계정을 이용해서 관리자 콘솔에 로그인한다.

관리자 콘솔에서 권한 부여 서버 구성을 시작할 수 있다! 

권한 부여 서버가 노출하는 OAuth 2 엔드포인트에 관한 세부 정보를 보고 싶으면  Realm(왕국이라는 뜻) Settings 에서 
OpenID Endpoint Configuration 를 클릭하면 된다.

토큰 공급자주소, 인증 엔드포인트, jwks_uri 등등 상세한 정보를 확인할 수 있다. 495 p 

토큰의 수명을 설정하고 싶다면 관리자 세팅 Tokens 탭에서 수명을 설정하면 된다. 실제 프로덕션 환경에서는 긴 수명을 
부여하지않아야한다. 테스트에서는 길게 활성화 해도 된다. 
```
### 권한 부여 서버 구성하기
```
1. 시스템에 클라이언트를 등록한다. OAuth 2 시스템에는 권한 부여 서버가 인식하는 클라이언트가 하나 이상 필요하다.
2. 클라이언트의 범위를 정의한다. 클라이언트 범위는 시스템에서 클라이언트의 목적을 식별하기 위함이다. 클라이언트
범위 정의로 권한 부여 서버가 발행하는 액세스 토큰을 맞춤구성 할 수 있다.

3. 애플리케이션에 사용자를 추가한다. 리소스 서버의 엔드포인트를 호출하기 위한 사용자
4. 사용자의 역할과 맞춤형 액세스 토큰을 정의한다. 사용자를 추가한 후에 액세스 토큰을 발행할 수 있다.
```

## 키클록 시스템에 클라이언트를 등록하고 사용자 설정하기
```
* CreateRealm 및 클라이언트 등록하기

우선 새로운 Realm 을 생성하기 위해 좌측 상단의 탭에서 CreateRealm 으로 생성한다. 그리고 왼쪽 Clients 탭에서 
클라이언트 아이디로 인증 서버 클라이언트를 생성할 수 있다.

클라이언트를 생성하면서 Valid redirect URIs 탭에 리소스 서버로 사용할 애플리케이션의 URL 주소를 설정한다. 
```
### 역할 정의하기 
```
키 클록 인증 서버에서는 두 가지 역할을 구현해야 한다. 역할에는 RealmRole 과 ClientRole 이 있고 두 가지를 모두 구현한다.

1. Realm Role: It is a global role, belonging to that specific realm. This can access from any client and 
map it to any user. 
Global Admin and Admin roles can be considered examples of this.
2. Client Role: It is a role which belongs only to that specific client. These roles cannot be accessed from
a different client.
This can only map to the users from that client. Example Roles: Employee, User etc.

출처: https://medium.com/geekculture/using-keycloak-with-spring-boot-3-0-376fa9f60e0b

간단하게 설명하자면 Realm Role 은 전체 인증 서버에서 사용하는 글로벌 역할이되고 Client Role 은 클라이언트 내에서 사용하는 세부적인
역할이 된다. 
(포괄적인 글로벌 롤을 만들고 클라이언트 내에서 세부적으로 에딧 한다고 생각하면 된다.)

Realm Role -> fitness_adminm, fitness_user
client Role -> admin, user 

글로벌 롤은 왼쪽 탭의 Realm roles 에서 생성할 수 있고, 클라이언트 내의 롤은 클라이언트 내의 롤에서 생성할 수 있다.
그리고 글로벌 롤 탭의 AssociatedRoles 에서 글로벌 롤과 클라이언트 롤을 서로 매핑시켜서 복합롤로 만들어주면 된다.

https://medium.com/geekculture/using-keycloak-with-spring-boot-3-0-376fa9f60e0b
자세한 내용은 위 출처를 참고한다. 
```
### 사용자 추가 및 액세스 토큰 얻기

인증서버에 리소스를 소유한 사용자를 생성한다.

```
* 사용자 추가

사용자는 왼쪽 Users 탭에서 생성할 수 있다. 사용자를 추가할 때는 고유한 사용자 이름을 지정하고 이메일이 확인 되었음을 지정하는
Email Verified 옵션을 체크한다. 

이메일 인증 외에 Required User Actions 옵션으로 사용자를 인증하는 부가적인 방법을 선택할 수 있다. 
(예제 진행을 위해 옵션을 선택하지 않는다. 유저 요청에 리액션이 있으면 사용자를 바로 인증에 사용할 수 없다. ) 

간단하게 사용자를 추가했으면 비밀번호를 설정해야 한다.(예제를 위해 직접 사용자 암호를 설정함, 보통 사용하는 측에서 만듦)

암호를 설정할 때 Temporary 로 설정하면 임시 암호가 돼서 처음 로그인시 암호를 변경하도록 하는 조치를 자동으로 추가하기 때문에
사용자 인증 테스트를 할 수 없게 되므로 언체크 한다. 

그리고 각 사용자의 롤 매핑탭에서 앞서 구현한 롤을 부여한다. 
```
```
* 사용자 인증 후 액세스 토큰 요청하기 

curl -XPOST "http://localhost:8080/realms/master/protocol/openid-connect/token" \
-H "Content-Type: application/x-www-form-urlencoded" \
--data-urlencode "grant_type=password" \
--data-urlencode "username=rachel" \
--data-urlencode "password=12345" \
--data-urlencode "scope=fitnessapp" \
--data-urlencode "client_id=fitnessapp" \

요청 테스트는 포스트 맨을 사용했다. 
(포스트맨 사용시 Authorization 탭의 OAuth 2.0 을 이용한다. )
https://jwt.io/ (토큰 값 확인 사이트)

키클록이 제공하는 API 에대해 알고 싶다면 Realm settings 탭의 맨 하단 엔드포인트를 참고한다. 
```
## 리소스 서버 구현하기 
```
* 제약 조건

컨트롤러 계층에서는 관리자만 DELETE 엔드 포인트에 접근할 수 있게 한다.
서비스 계층에서는 사용자가 자기 운동 기록만 추가할 수 있게 한다.
리포지토리 계층에서는 인증된 사용자의 운동 기록만 검색할 수 있게 한다. 
```
### 스프링 시큐리티 리소스 서버 설정
```
* JwtAuthConverter 구현하기

리소스 소유자가 리소스서버의 엔드포인트를 호출하려면 토큰 값을 헤더에 포함시켜 요청한다. 리소스 서버는 해당 토큰을 
JwtAuthenticationToken 으로 컨버팅해서 시큐리티 컨텍스트 홀더에 저장한다. 

컨버터를 직접 구현하지 않아도 되지만 토큰을 컨버팅하는 기본 설정을 바꾸고 싶거나 토큰에 추가된 값이 있어서 Authorities 정보를
커스텀하고 싶다면 직접 컨버터를 구현할 수 있다.

앞서 Client Role 추가한 것을 JwtAuthenticationToken 의 Authorities 에 추가하기 위해 커스텀 컨버터를 구현한다. 

JwtAuthConverter, JwtAuthConverterProperties, yml 참고 
```
```
* 리소스 서버 스프링 시큐리티 구현하기

ResourceServerConfig 참고 

시큐리티 7.0 에서 바뀔 시큐리티 DSL 

 @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/blog/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .permitAll()
            )
            .rememberMe(Customizer.withDefaults());

        return http.build();
    }
    
자세한 내용은 https://docs.spring.io/spring-security/reference/migration-7/configuration.html 참고
```

### 시큐리티 웹 보안식 
```
* @EnableMethodSecurity (전역 메서드 보안) 

애노테이션을 구성 클래스에 추가하면 @PreAuthorize, @PostAuthorize 애노테이션과 SpEL 로 권한 부여 규칙을 지정할 수 있다.

https://github.com/eternalrecurrenceofthesame/Spring-security-in-Action/tree/main/ch16 참고
```
```
* 스프링 데이터에서 jpql 에 SpEL 사용하기

시큐리티 구성 클래스에 SecurityEvaluationContextExtension 빈을 추가하면 스프링 데이터에서도 SpEL 을 사용할 수 있다.
(의존성 추가 필요)

ResourceServerConfig, WorkoutRepository 참고 
```
### 리소스 서버 yml  설정하기
```
yml 설정 값으로는 JwtAuthConverter 에서 사용할 JwtAuthConverterProperties 와 자바 설정으로 구성하는 대신
인가 서버의 발행자 주소 및 서명키 설정을 구현한다. 

yml 참고 
```
#### + schema.sql, data.sql 
```
스프링 부트 3.0 기준 schema 는 제대로 작동하지만 data 는 제대로 작동하지 않기 때문에 data.sql 을 사용하는 대신
직접 데이터베이스에 값을 쿼리해야 한다.

data.sql 쿼리 참고 
```
### 리소스 서버 + 권한부여 서버 테스트하기 
