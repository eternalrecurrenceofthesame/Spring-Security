package com.spring.securitytest.test;

import com.spring.securitytest.test.testconfig.WithCustomUser;
import com.spring.securitytest.testdata.ex2.service.NameService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;


import javax.naming.AuthenticationException;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @SpringBootTest 애플리케이션을 구성하는 빈을 모두 스프링 컨텍스트에 올린 후 테스트를 진행한다.
 * @AutoConfigureMockMvc 스프링 부트가 MockMvc 를 자동 구성하게 한다. MockMvc 형식의 객체가 컨텍스트에 추가됨.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MainTest {

    @Autowired
    private MockMvc mvc; // MockMvc 기능을 주입 받아서 사용한다.

    @Autowired
    private NameService nameService; // 메서드 보안 테스트에 사용

    /**
     * 모의 사용자 테스트, 시큐리티 컨텍스트에 저장된 사용자를 단위 테스트 한다.
     */
    @DisplayName("인증되지 않은 사용자의 엔드 포인트 호출")
    @Test
    public void helloUnauthenticated() throws Exception {

        mvc.perform(get("/hello"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("애노테이션을 사용해서 모의 사용자 테스트")
    @Test
    @WithMockUser(username = "mary") // 애노테이션으로 테스트 사용자 구성
    public void helloAuthenticated() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(content().string("Hello, mary!"))
                .andExpect(status().isOk());
    }

    @DisplayName("RequestPostProcessor 를 사용한 모의 사용자 테스트")
    @Test
    public void helloAuthenticationWithUser() throws Exception {
        mvc.perform(get("/hello").with(user("mary")))
                .andExpect(content().string("Hello, mary!"))
                .andExpect(status().isOk());
    }

    /**
     * UserDetails 에서 실제 데이터를 보안 컨텍스트로 가져와서 테스트하는 통합 테스트 진행
     */
    @DisplayName("사용자 통합 테스트")
    @Test
    @WithUserDetails("user")
    public void helloAuthenticated2() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

    /**
     * 맞춤형 인증 Authentication 객체를 이용한 테스트, testconfig 참고
     *
     * 맞춤형 인증 Authentication 을 이용하는 방식도 @WithMockUser, @WithUserDetails 와 마찬가지로
     * 인증 논리를 건너뛰기 때문에 프로바이더를 거치지 않는다 571 p
     */
    @DisplayName("맞춤형 Authentication 객체 테스트")
    @Test
    @WithCustomUser(username = "mary")
    public void helloAuthenticated3() throws Exception {
        mvc.perform(get("/hello"))
                .andExpect(status().isOk());
    }

    /**
     * 메서드 보안 테스트 테스트 진행을 위해 ex2 패키지의 값을 자동 구성으로 사용한다.
     */
    @DisplayName("메서드 보안 테스트 인증되지 않은 유저의 메서드 접근")
    @Test
    void testNameServiceWithNoUser(){
        assertThrows(AuthenticationCredentialsNotFoundException.class, ()-> nameService.getName());
    }

    @DisplayName("메서드 보안 테스트 잘못된 권한 read")
    @Test
    @WithMockUser(authorities = "read")
    void testNameServiceWithUserButWrongAuthority(){
        assertThrows(AccessDeniedException.class, () -> nameService.getName());
    }

    @DisplayName("메서드 보안 테스트 올바른 권한 write")
    @Test
    @WithMockUser(authorities = "write")
    void testNameServiceWithUserButCorrectAuthority(){
        var result = nameService.getName();

        assertEquals("Fantastico", result);
    }




}
