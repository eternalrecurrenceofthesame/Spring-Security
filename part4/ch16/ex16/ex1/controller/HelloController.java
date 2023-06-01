package com.spring.securityaspect.ex1.controller;

import com.spring.securityaspect.ex1.service.NameService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
//@RestController
public class HelloController {

    private NameService nameService;

    @GetMapping("/secret/names/{name}")
    public List<String> names(@PathVariable String name){
        return nameService.getSecretNames(name);
    }


}
