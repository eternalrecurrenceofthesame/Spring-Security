package com.spring.reactivesecurity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;



@RestController
public class HelloController {

    @GetMapping("/hello")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<String> hello(Mono<Authentication> auth){
        Mono<String> message = auth.map(a -> "Hello " + a.getName());

        return message;
    }

    @GetMapping("/helloreactive")
    public Mono<String> hello(){
        Mono<String> message = ReactiveSecurityContextHolder.getContext() // 리액티브 보안 홀더를 꺼낸다.
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> "Hello " + auth.getName());

        return message;
    }
}
