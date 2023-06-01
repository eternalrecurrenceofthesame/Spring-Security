package com.spring.securitytest.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ex 1 패키지를 이용하는 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    /**
     * httpBasic() RequestPostProcessor 를 이용한 인증 테스트
     */
    @DisplayName("사용자 인증 요청")
    @Test
    public void helloAuthenticatingWithValidUser() throws Exception {
        mockMvc.perform(get("/hello")
                .with(httpBasic("user","12345")))
                .andExpect(status().isOk());
    }



}
