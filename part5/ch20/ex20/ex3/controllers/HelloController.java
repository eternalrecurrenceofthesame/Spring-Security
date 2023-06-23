package com.spring.securitytest.testdata.ex3.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/hello")
    public String home() {
        return "Hello!";
    }
}
