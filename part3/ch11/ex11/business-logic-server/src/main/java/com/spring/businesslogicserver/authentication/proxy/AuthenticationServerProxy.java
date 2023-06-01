package com.spring.businesslogicserver.authentication.proxy;

import com.spring.businesslogicserver.authentication.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 인증 공급자가 사용하는 Rest 서비스, 인증 공급자는 인증 서버를 호출해서 클라이언트를 인증한다.
 * 이때 사용하는 Rest 서비스를 정의한 클래스
 *
 * 프록시로 지칭한 이유는 비즈니스 로직 서버에서 실제로 호출하는 클래스가 아니기 때문인듯?
 */

@Slf4j
@Component
public class AuthenticationServerProxy {

    private RestTemplate rest;

    /**
     * @Value 사용시 롬복 의존성 주입 애노테이션 사용 불가능
     */
    @Value("${auth.server.base.url}")// properties 에서 호출할 API 의 URL 을 얻는다.
    private String baseUrl;

    @Autowired
    public AuthenticationServerProxy(RestTemplate rest) {
        this.rest = rest;
    }

    /**
     * 첫 번째 인증
     */
    public void sendAuth(String username, String password){

        String url = baseUrl + "/user/auth";

        var body = new User();
        body.setUsername(username);
        body.setPassword(password);


        var request = new HttpEntity<>(body); // 바디 담기

        rest.postForEntity(url, request, Void.class);
    }

    /**
     * 두 번째 OTP 인증
     */
    public boolean sendOTP(String username, String code){

        String url = baseUrl + "/otp/check";

        var body = new User();
        body.setUsername(username);
        body.setCode(code);

        var request = new HttpEntity<>(body);

        var response = rest.postForEntity(url, request, Void.class); // ResponseEntity 값을 반환한다.

        return response.getStatusCode().equals(HttpStatus.OK);
    }


}
