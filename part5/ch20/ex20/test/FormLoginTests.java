package com.spring.securitytest.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FormLoginTests {

    @Autowired
    private MockMvc mvc;

    @DisplayName("실패 핸들러")
    @Test
    public void loggingInWithWrongUser() throws Exception {
        mvc.perform(formLogin()
                .user("nouser").password("12345")) // 로그인에 잘못된 자격 증명을 사용한다.
                .andExpect(header().exists("failed"))
                .andExpect(unauthenticated());
    }

    @DisplayName("권한 없는 유저")
    @Test
    public void loggingInWithWrongAuthority() throws Exception {
        mvc. perform(formLogin().user("mary").password("12345"))
                .andExpect(redirectedUrl("/error"))
                .andExpect(status().isFound())
                .andExpect(authenticated());
    }

    @DisplayName("권한 있는 유저")
    @Test
    public void loggingInWithCorrectAuthority() throws Exception {
        mvc.perform(formLogin().user("bill").password("12345"))
                .andExpect(redirectedUrl("/home"))
                .andExpect(status().isFound())
                .andExpect(authenticated());
    }
}
