package springsecurity.ssia.ch8.basic.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class HelloController {

    /**
     * 어드민만 호출 가능
     */
    @GetMapping("/hello")
    public String hello(){
        return "Hello!";
    }

    /**
     * 매니저만 호출 가능
     */
    @GetMapping("/ciao")
    public String ciao(){
        return "Ciao!";
    }

    @GetMapping("/hola")
    public String hola(){
        return "Hola";
    }
}
