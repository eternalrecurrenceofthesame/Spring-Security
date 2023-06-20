package springsecurity.ssia.ch5.mode_inheritablethredlocal.controller;

import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class ByeController {

    @GetMapping("/hello")
    @Async
    public String hello() {
      SecurityContext context = SecurityContextHolder.getContext();
      Authentication a = context.getAuthentication();

        return "Hello, " + a.getName() + "!";
    }

    /**
     * @Async 를 사용해서 메서드가 별도의 스레드에서 실행되게 설정했다.
     * 그러면 메서드를 실행하는 스레드와 요청을 수행하는 스레드가 서로 다르게 된다.
     */
    @GetMapping("/bye")
    @Async
    public String goodbye() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication a = context.getAuthentication();

        return "bye" + a.getName();
    }

}
