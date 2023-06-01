# 사용자 관리
```
견고한 프레임워크는 계약을 통해서 프레임워크의 구현과 이에 기반을 둔 애플리케이션을 분리한다.
(인터페이스와 구현체의 계약 관계)

프로그래머는 프레임워크의 추상화를 알고 이를 이용해 통합한다. 
```
## 스프링 시큐리티의 인증 구현

```
* 사용자 관리를 위한 인터페이스

UserDetailsService - 사용자 이름으로 사용자 검색
UserDetailsManager - 사용자 추가, 수정 삭제 

두 계약을 분리하는 것은 ISP 의 훌륭한 예시다!
ISP 란? -> 특정 클라이언트를 위한 인터페이스 여러 개가 범용 인터페이스 하나보다 낫다는 뜻

인터페이스를 분리해서 필요 없는 동작을 구현하도록 강제하지 않는다.(유연성 향상)
```

UserDetailsManager -> UseDetailsServie -> UserDetails(사용자) <- GrantedAuthority

UserDetails 계약으로 사용자를 구현하고 확장한다 (service, manager)

GrantedAuthority 는 하나 이상의 권한으로 구현된다, 사용자는 하나 이상의 Granted 계약을 가진다 

## 사용자 기술하기

프레임워크가 사용자를 인식할 수 있게 사용자를 구현하는 것은 인증 흐름을 구축하기 위한 필수 단계

어플리케이션 사용자에 따라 권한이 부여되기 때문. 사용자 정의는 UserDetails 계약을 준수한다.

```
* UserDetails 계약의 정의 이해하기

UserDetails 계약

private final String username;
private final String password;
private final String authority;

String getUsername();
String getPassword(); // 사용자 자격 증명을 반환하는 메서드 

Collections<? extends GrantedAuthority> getAuthorites(); // 권한을 리스트로 만들어서 반환하는 메서드

  @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(()-> authority);  이렇게 만들어짐
    }
    

boolean isAccountNonExpired();
boolean isAccountNonLocked();
boolean isCredntialsNonExpired();
boolean isEnabled(); // 사용자 계정을 필요에따라 활성 또는 비활성하는 메서드 네 개 

메서드 구현이 필요 없으면 단순  true 를 반환하면 된다. DummyUser 클래스 참고.
```
```
* GrantedAuthority(권한) 계약 살펴보기

권한은 사용자가 애플리케이션에서 수행할 수 있는 작업을 나타낸다.

GrantedAuthority 계약과 SimpleGrantedAuthority 클래스를 이용해서 권한 만들기

GrantedAuthority g1 = () -> "READ";
GrantedAuthority g2 = new SimpleGrantedAuthority("READ");

Tip 73p
람다식을 만들 때는 @FunctionalInterface 애노테이션을 사용해서 인터페이스가 함수형임을 지정해주는 것이 좋다.
```
```
* 최소한의 UserDetails 구현 작성 

DummyUser 클래스 참고 
```
```
* 빌더를 이용한 UserDetails 형식의 인스턴스 만들기

User.UserBuilder builder1 = User.withUsername("이름");

        UserDetails u1 = builder1.password("12345")
                .authorities("read", "write")
                .passwordEncoder(p -> encode(p))
                .accountExpired(flase)
                .disabled(true)
                .build();

여기서 사용한 인코더는 앞서 살펴본 PasswordEncoder 계약과 달리 단순 지정한 인코딩으로
변환하는 일만 수행한다. 77p 

별도로 암호 인코딩 함수를 지정해서 인코딩한 후 UserDetails 인스턴스를 반환할 수 있다. 
```
### 사용자와 연관된 여러 책임의 결합 
```
User 를 데이터베이스에 저장하고 데이터를 다른 애플리케이션으로 전송할 때 사용할 클래스와
스프링 시큐리티가 사용자를 이해할 수 있게 UserDetails 클래스를 만들어보자.

보통 하나의 유저는 여러 개의 권한을 가지는 경우가 많다. 여기서는 간단히 하나의 권한을 가진
User 엔티티를 구현했다.

애플리케이션 유지 관리성을 높이기 위해 두 가지 책임을 혼합하지 말고 분리해서 코드를 작성해야 한다.

SecurityUser(UserDetails) 클래스는 스프링 시큐리티에서 사용자 세부 정보를 이해하는 클래스이고
User 클래스는 엔티티 클래스로 데이터베이스에 매핑되는 클래스이다. (사용자)

SecuritUser, User 참고
```

## 스프링 시큐리티가 사용자를 관리하는 방법 지정

스프링 시큐리티는 UserDetailsService 를 이용해서 사용자를 관리한다.

```
* UserDetailsService 계약의 이해

loadUserByUserame(String username) 하나의 메서드를 가진 인터페이스.

사용자 이름으로 주어진 이름을 가진 사용자의 UserDetails 정보를 시큐리티에서 얻는다.
인자로 받는 사용자 이름은 고유하다고 간주된다.
```
```
* UserDetailsService 계약 구현

InMemoryUserDetailsService, ProjectConfig 참고 
```
```
* UserDetailsManager 계약 구현

UserDetailsManager 는 UserDetailsService 의 확장팩이다 유저 생성 변경 삭제 중복네임 찾기 기능이 
추가되어 있다.
```

### 사용자 관리에 JdbcUserDetailsManager 이용
```
JdbcUserDetailsManger 는 SQL 데이터베이스에 저장된 사용자를 관리하며 JDBC 를 통해서 데이터베이스에
직접 연결한다.

인증공급자가 JdbcUserDetailsManger 를 호출하면 사용자 이름으로 데이터베이스에서 사용자 값을 가지고 온다.
사용자가 발견되면 암호 인코더는 사용자가 제공한 암호가 데이터베이스에 있는 암호와 일치하는지 확인한다. 88p

Tip
resources 하위에 schema.sql, data.sql 을 이용해서 어플리케이션 실행 직전에 테이블과 데이터를 생성할 수 있다.
임베디드 데이터베이스를 사용할 때 유용하다! schema, data 참고 

스프링 부트 2.5 부터 자동 sql 생성 기능을 사용하려면 spring.jpa.defer-datasource-initialization=true 
설정을 해줘야 한다.
```
```
* JdbcUserDetailsManager 등록하기

ex3-ProjectConfig 참고 앞서 설명했지만 UserDetailsService 는 이름으로 유저 정보를 찾아온다. 그래서 Jdbc 를 이용할 때도
이름으로 쿼리하면 된다.
```
### 사용자 관리에 LdapUserDetailsManger 사용

LDAP(Lightweight Directory Access Protocol)는 사용자가 조직, 구성원 등에 대한 데이터를 찾는 데 도움이 되는 프로토콜이다.

실습에서는 실제 LDAP 서버를 사용할 수 없으므로 server.ldif 를 이용해서 임베디드 서버를 만들어서 사용한다. 
```
JdbcUserDetailsManager 보다 덜 이용된다!

LDAP 임베디드 구성 application.properits 참고
LDAP 임베디드 서버 server.ldif 참고 

ProjectConfigLdap 참고
```





