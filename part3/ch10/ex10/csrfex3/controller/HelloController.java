package com.securty.csrfex3.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String postHello(){
        return "Post Hello!";
    }

    @PostMapping("/ciao")
    public String postCiao(){
        return "Post Ciao";
    }
}
