package springsecurity.ssia.ch5.mode_thredlocal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;

//@RestController
public class HelloController {


    @GetMapping("/hello")
    public String hello(Authentication a){
        return "Hello, " + a.getName() + "!";
    }


}
