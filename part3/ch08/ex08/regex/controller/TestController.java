package springsecurity.ssia.ch8.regex.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/email/{email}")
    public String email(@PathVariable String email) {
        return "이메일: " + email;
    }

    @GetMapping("/video/{country}/{language}")
    public String video(@PathVariable String country,
                        @PathVariable String language){
        return "유튜브 정보: " + country + " " + language;
    }
}
