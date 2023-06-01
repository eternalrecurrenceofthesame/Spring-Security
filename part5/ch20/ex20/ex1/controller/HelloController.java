package com.spring.securitytest.testdata.ex1.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


//@RestController
public class HelloController {

    /**
     * 인증 객체는 인증 컨텍스트에서 주입 받아서 사용할 수 있다 ch 5 참고
     */
    @GetMapping("/hello")
    public String hello(Authentication a){
        return "Hello, " + a.getName() + "!"; // 맞춤형 인증 테스트를 위해 잠시 주석 처리.
    }


}
