package com.spring.singlesignon.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Slf4j
@Controller
public class MainController {

    /**
     * 스프링 부트는 사용자를 나타내는 Authentication 객체를 자동으로 메서드의 매개 변수에 주입해준다.
     */
    @GetMapping("/")
    public String main(OAuth2AuthenticationToken token){
        log.info(String.valueOf(token));
        return "main.html";
    }



}
