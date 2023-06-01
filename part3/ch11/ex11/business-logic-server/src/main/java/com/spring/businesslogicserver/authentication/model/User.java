package com.spring.businesslogicserver.authentication.model;

import lombok.Data;

/**
 * 인증 서비스의 REST API 를 호출할 때 넘겨주는 User 데이터
 */
@Data
public class User {

    private String username;
    private String password;
    private String code; // otp 코드

}
