package springsecurity.ssia.ch9.previous.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(){
        return "hello!";
    }
}
