package com.spring.authenticationserver.controller;

import com.spring.authenticationserver.entity.Otp;
import com.spring.authenticationserver.entity.User;
import com.spring.authenticationserver.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@AllArgsConstructor
@RestController
public class AuthController {

    private UserService userService;

    /** 유저 등록 */
    @PostMapping("/user/add")
    public void addUser(@RequestBody User user){
        userService.addUser(user);
    }

    /** 1차  유저 인증 */
    @PostMapping("/user/auth")
    public void auth(@RequestBody User user){
        log.info("user 매핑 확인 username: " + user.getUsername());
        log.info("user 매핑 확인 password: " + user.getPassword());

        userService.auth(user);
    }

    /** 2차 OTP 인증 */
    @PostMapping("/otp/check")
    public void check(@RequestBody Otp otp, HttpServletResponse response){
        if(userService.check(otp)){
            response.setStatus(HttpServletResponse.SC_OK);
        }else{
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 2차 인증 실패시 권한 없음
        }
    }
}
