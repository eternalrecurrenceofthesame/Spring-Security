package com.spring.authenticationserver.util;

import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Otp 값을 생성할 때 사용하는 클래스
 */
@Component
public final class GenerateCodeUtil {

    private GenerateCodeUtil(){}

    public static String generateCode(){
        String code;

        try{
            SecureRandom random = SecureRandom.getInstanceStrong(); // 임의의 int 값을 생성

            // 0 ~ 8999 사이의 값을 생성하고 1000 을 더해서 1000 ~ 9999 (4 자리 임의 코드) 사이의 값을 얻는다.
            int c = random.nextInt(9000) + 1000;

            code = String.valueOf(c); // int 를 String 으로 변환
        } catch(NoSuchAlgorithmException e){
            throw new RuntimeException("OTP 코드를 생성하는 데 문제가 생겼습니다.");
        }
        return code;
    }
}
