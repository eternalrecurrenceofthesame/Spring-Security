package springsecurity.ssia.ch5.delegating.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.concurrent.DelegatingSecurityContextCallable;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.concurrent.DelegatingSecurityContextScheduledExecutorService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;

//@RestController
@Slf4j
public class DelegatingController {

    @GetMapping("/ciao")
    public String ciao() throws ExecutionException, InterruptedException {

        Callable<String> task = () -> {
            SecurityContext context = SecurityContextHolder.getContext();

            System.out.println(context.getAuthentication().getName());
            return context.getAuthentication().getName();
        };

        ExecutorService e = Executors.newCachedThreadPool();


        try{
            var contextTask = new DelegatingSecurityContextCallable<>(task);
            return "Ciao, " + e.submit(contextTask).get() + "!";
        }finally{
            e.shutdown();
        }
    }

    @GetMapping("/hola")
    public String hola() throws ExecutionException, InterruptedException {
        Callable<String> task = () -> {
            SecurityContext context = SecurityContextHolder.getContext();
            return context.getAuthentication().getName();
        };

        ExecutorService e = Executors.newCachedThreadPool();

        e = new DelegatingSecurityContextExecutorService(e);

        try{
            return "Hola, " + e.submit(task).get() + "!";
        }finally{
            e.shutdown();
        }

    }


}
