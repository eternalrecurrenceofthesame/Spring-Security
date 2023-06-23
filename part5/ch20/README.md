# 스프링 시큐리티 테스트

단위 테스트 및 통합 테스트를 작성하는 주된 이유는 버그를 수정하거나 새 기능을 구현하면서 기존 기능이 망가지지 않는지 확인하기 위함이다. 이를 회귀 테스트라고 한다.

```
애플리케이션을 테스트 할 때는 자신의 애플리케이션 코드 뿐만 아니라 이용하는 프레임 워크 및 라이브러리와의 통합 테스트도 필요하다
프레임워크나 라이브러리는 계속해서 새로운 버전이 나온며, 새로운 버전과 기존의 코드들이 잘 통합되는지 확인해야 한다. 
```
## 1. 모의 사용자로 사용자 단위 테스트 

단위 테스트를 하기 위해서는 테스트가 전체 애플리케이션 구조에서 어느 부분에 위치하고 동작하는지를 잘 파악해야 한다. 558 p

```
모의 사용자를 이용해서 권한 부여 구성을 테스트 하는 방법을 알아보자 모의 사용자를 이용하는 테스트에서는 인증 프로세스는 완전히 건너뛴다.
한마디로 보안 컨텍스트에 저장된 사용자만 단위 테스트 한다는 의미.

모의 사용자를 만드는 방법은 @WithMockUser 애노테이션을 사용하거나, RequestPostProcessor 로 보안 환경을 정의하는 방법이 있다.

애노테이션을 사용하면 먼저 테스트가 보안 환경을 설정한다(테스트가 먼저 모의 사용자를 만든다는 의미), 
RequestPostProcessor 를 사용하면 테스트 요청이 생성된 후 테스트 보안 환경 등의 다른 제약 조건을 정의(모의 사용자)하기 위해 변경된다. 
```
```
* 테스트에 사용할 엔드포인트

 @GetMapping("/hello")
    public String hello(Authentication a){
        return "Hello, " + a.getName() + "!";
    }
```
```
* @WithMockUser

   @DisplayName("애노테이션을 사용해서 모의 사용자 테스트")
    @Test
    @WithMockUser(username = "mary") // 애노테이션으로 테스트 사용자 구성
    public void helloAuthenticated() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(content().string("Hello, mary!"))
                .andExpect(status().isOk());
    }

@WithMockUser 로 모의 사용자를 만들고 엔드포인트가 호출되는지 테스트한다.
전체 테스트 구조는 MainTest 참고 
```
```
* RequestPostProcessor

 @DisplayName("RequestPostProcessor 를 사용한 모의 사용자 테스트")
    @Test
    public void helloAuthenticationWithUser() throws Exception {
        mvc.perform(get("/hello").with(user("mary")))
                .andExpect(content().string("Hello, mary!"))
                .andExpect(status().isOk());
    }
```

## 2. UserDetailsService 로 사용자 조회 테스트 

가짜 사용자를 만드는 대신 UserDetailsService 에서 사용자를 조회하는 통합 테스트 만들어보기
```
UserDetailsService 로 사용자를 조회하고 조회한 사용자(user) 로 권한 부여 테스트를 진행한다.
@WithUserDetials("user") 애노테이션을 사용하면 사용자 세부 정보에서 사용자를 쉽게 가져올 수 있다.
```
```
* ex 1 config 에 등록 되어 있는 사용자 정보 

  @Bean
    public InMemoryUserDetailsManager userDetailsService(){
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("12345")
                .roles("user")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
```
```
* @WithUserDetials 사용

  @DisplayName("사용자 통합 테스트")
    @Test
    @WithUserDetails("user")
    public void helloAuthenticated2() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

전체적인 구조는 MainTest 를 참고한다.
```

## 3. 맞춤형 인증 Authentication 객체를 이용한 테스트 

모의 사용자를 테스트 할 때는 프레임워크가 Authentication 인스턴스를 생성해준다. 
```
컨트롤러에 Authentication 객체에 의존하는 논리가 있다고 가정하면 테스트를 위한 Authentication 객체를 특정한 형식으로
만들어서 사용해야 할 수 있다. 

테스트를 위한 특제 Authentictiaon 객체를 프레임 워크에 만들어 달라고 지시해보자! 
```
### 맞춤형 Authetnication 객체를 만드는 프로세스
```
1. 맞춤형 애노테이션을 정의한다. 

테스트를 위한 맞춤형 애노테이션을 정의해서 Authentication 객체를 만드는 데 필요한 세부 정보를 애노테이션의 속성으로 
정의할 수 있다.   

애노테이션의 보존 정책을 런타임으로 설정하도록 @Retention(RetentionPolicy.RUNTIME) 을 지정해야 런타임에 자바 리플렉션을
이용해 맞춤형 애노테이션을 읽어들일 수 있다.

WithCustomUser 참고 
```
```
2. 모의 SecurityContext 를 위한 팩터리 클래스 작성하기

테스트를 실행할 때 사용할 SecurityContext 를 만들어야 한다. 여기서 테스트에 어떤 종류의 Authentication 을 사용할지 결정한다.
CustomSecurityContextFactory 참고
```
```
3. 맞춤형 애노테이션을 팩토리 클래스에 연결

WithCustomUser 애노테이션 클래스에 @WithSecurityContext(fatcory = CustomSecurityContextFactory.class) 를 지정해서 애노테이션과
팩토리 클레스를 연결한다. 
```
```
* 테스트 작성 MainTest 참고

참고로 맞춤형 인증 Authentication 을 이용하는 방식도 @WithMockUser, @WithUserDetails 와 마찬가지로 인증 논리를 건너뛰기 때문에 
프로바이더를 거치지 않는다 571 p 
```

## 4. 메서드 보안 테스트

전역 메서드 보안으로 스프링 시큐리티를 이용하는 경우를 가정한다. 쉽게 말해서 메서드 보안이 잘 적용되고 있는지 테스트 하는 것임.

```
* 테스트 시나리오

인증된 사용자 없이 메서드를 호출하면 메서드가 AuthenticationCredentialsNotFoundException 을 투척한다. 
필요한 권한(쓰기) 이 아닌 다른 권한을 가진 인증된 사용자로 호출하면 메서드가 AccessDeniedException 을 투척한다

예상된 권한을 가진 인증된 사용자로 호출하면 메서드가 예상된 결과를 반환한다.
```
### 메서드 보안 테스트 작성 
```
* 호출할 보안 메서드 구조 (컨트롤러 -> 서비스)

전체적인 구조는 ex2 예시를 참고한다. 

- controller

    @Autowired
    private NameService nameService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, " + nameService.getName();
    }

- service

  @PreAuthorize("hasAuthority('write')")
    public String getName() {
        return "Fantastico";
    }
```
```
* 인증되지 않은 사용자 

    @DisplayName("메서드 보안 테스트 인증되지 않은 유저의 메서드 접근")
    @Test
    void testNameServiceWithNoUser(){
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()-> nameService.getName());
    }
```
```
* 잘못된 권한

   @DisplayName("메서드 보안 테스트 잘못된 권한 read")
    @Test
    @WithMockUser(authorities = "read")
    void testNameServiceWithUserButWrongAuthority(){
        assertThrows(AccessDeniedException.class, () -> nameService.getName());
    }
```
```
* 올바른 권한

    @DisplayName("메서드 보안 테스트 올바른 권한 write")
    @Test
    @WithMockUser(authorities = "write")
    void testNameServiceWithUserButCorrectAuthority(){
        var result = nameService.getName();

        assertEquals("Fantastico", result);
    }
```

## 5. 인증 테스트
```
인증 논리 구현을 전체적으로 테스트한다 
인증필터 -> 관리자 -> 공급자 -> 유저 세부 정보 -> 인코더 -> 컨텍스트로 이어지는 일련의 과정을 테스트하는 통합테스트
```
### 인증 테스트 구현하기
```
* httpBasic() RequestPostProcessor 를 이용한 테스트 

맞춤형 인증 공급자와 유저 상세 서비스를 테스트한다. 

공급자에서 인증 논리를 검증하는 유스케이스와, 일반 공급자가 유저 상세 서비스를 호출하는 유스케이스 두 가지를 테스트 한다.
ex 20 ex1 CustomAuthenticationProvider, AuthenticationTests 참고
```
```
* fromLogin() 양식 테스트 ex 3, FormLogin  참고

html formLogin 을 사용하면 로그인 자격 증명의 성공,실패에 따라 맞춤형 핸들러를 적용할 수 있다 
```
## 6. CSRF 구성 테스트

GET 요청 이외의 다른 요청을 테스트해야 할 때 CSRF 보호가 작동하는지 테스트 해보기! 
```
* 테스트 예시 post 요청에는 csrf 토큰이 필요하다.

@Test
public void testHelloPOSTWithCSRF() throws Exception{
mvc.perform(post("/hello").with(csrf()))
.andExpect(status().isOk());
}}
```
## 7. CORS 구성 테스트

CORS 에 대한 자세한 내용은 아래 링크를 참고한다. 

https://github.com/eternalrecurrenceofthesame/Spring-security-in-action/tree/main/part3/ch10

```
* 자바 설정

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
```
* 테스트


    @Test
    @DisplayName("Test CORS configuration for /test endpoint")
    public void testCORSForTestEndpoint() throws Exception {
        mvc.perform(options("/test")
                .header("Access-Control-Request-Method", "POST")
                .header("Origin", "http://www.example.com")
        )
        .andExpect(header().exists("Access-Control-Allow-Origin"))
        .andExpect(header().string("Access-Control-Allow-Origin", "*"))
        .andExpect(header().exists("Access-Control-Allow-Methods"))
        .andExpect(header().string("Access-Control-Allow-Methods", "POST"))
        .andExpect(status().isOk());
    }

```
## 8. 리액티브 스프링 시큐리티 구현 테스트 예시
```
* 테스트 엔드포인트

@RestController

    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello!");
    }
```
```
* 리액티브 구현 예시

@SpringBootTest
@AutoConfigureWebTestClient // 테스트에 이용할 WebTestClient 를 자동 구성
class MainTests{

@Autowired // 스프링 부트가 구성한 WebTestClient 인스턴스 삽입 
private WebTestClient client;

@Test
@WithMockUser // 모의 사용자 정의 
void testCallHelloWithValidUser(){
client.get() // 교환 수행 후 결과 검증 
.uri("/hello").exchange().expectStatus().isOk();
}}

```
```
* 모의 사용자를 정의한 테스트

@Test
void testCallHelloWithValidUserWithMockUser(){
client.mutateWith(mockUser()) // GET 요청 실행 전 모의 사용자를 이용하도록 호출
.get().uri("/hello").exchange()
.expectStatus().isOk();
}

client.mutateWith(csrf()) // POST 호출에 대한 CSRF 보호 테스트 
.post().uri("/hello").exchange()
.expectStatus().isOk();
```

