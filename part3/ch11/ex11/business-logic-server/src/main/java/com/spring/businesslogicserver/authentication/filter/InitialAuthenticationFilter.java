package com.spring.businesslogicserver.authentication.filter;

import com.spring.businesslogicserver.authentication.OtpAuthentication;
import com.spring.businesslogicserver.authentication.UsernamePasswordAuthentication;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class InitialAuthenticationFilter extends OncePerRequestFilter { // 한번만 호출되는 필터

    private AuthenticationManager manager; // 직접 만든 인증 관리자. 공급자를 재정의하면 관리자도 재정의 해야한다.

    /**
     * 속성 파일에서 JWT 토큰에 서명하는 데 이용할 키 값을 가져온다.
     */
    @Value("${jwt.signing.key}")
    private String signingKey;

    @Autowired
    public InitialAuthenticationFilter(AuthenticationManager manager) {
        this.manager = manager;
    }

    /**
     * 필터 체인이 이 필터에 도착할 때 호출되는 메서드
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String username = request.getHeader("username");
        String password = request.getHeader("password");
        String code = request.getHeader("code");


        if(code == null){
            /**
             * otp code 가 없으면 1 차 인증 요청 유저
             */
            Authentication a = new UsernamePasswordAuthentication(username, password);
            manager.authenticate(a);
        }else{
            /**
             * otp code 가 있으면 2 차 인증 요청 유저
             */
            Authentication a = new OtpAuthentication(username, code);
            manager.authenticate(a); // 매니저에 인증 객체를 인자로 전달한다.

            /**
             * JWT 를 구축하고 인증된 사용자의 사용자 이름을 클레임 중 하나로 저장한다.
             *
             * 바이트 코드로 인코딩한 JWT 키를 생성해서 서명으로 사용한다.
             */
            SecretKey key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));
            String jwt = Jwts.builder()
                    .setClaims(Map.of("username", username)) // JWT 본문 값 추가
                    .signWith(key) // 대칭키를 사용한 서명 첨부
                    .compact();

            /** 토큰을 HTTP 응답의 권한 부여 헤더에 추가한다 */
            response.setHeader("Authorization", jwt);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        /**
         * login 경로가 아니면 false 값을 반환한다 즉 로그인 경로만 이 필터를 적용한다는 의미
         *
         * 이 메서드를 재정의 하면 필터가 적용될지 결정하는 논리를 구현할 수 있다 ch9 참고
         */
        return !request.getServletPath().equals("/login");
    }
}
