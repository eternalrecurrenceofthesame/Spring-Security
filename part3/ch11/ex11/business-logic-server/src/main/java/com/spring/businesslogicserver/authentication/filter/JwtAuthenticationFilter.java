package com.spring.businesslogicserver.authentication.filter;

import com.spring.businesslogicserver.authentication.UsernamePasswordAuthentication;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Value("${jwt.signing.key}")
    private String signingKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String jwt = request.getHeader("Authorization");

        /** 토큰 키 가져오기 */
        SecretKey key = Keys.hmacShaKeyFor(signingKey.getBytes(StandardCharsets.UTF_8));

        /**
         * 토큰을 구문 분석해서 클레임을 얻고 서명을 검증한다. 서명이 유효하지 않으면 예외가 투척된다.
         */
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt) // 서명된 Jwt 를 Jws 라고 한다.
                .getBody();

        String username = String.valueOf(claims.get("username"));

        GrantedAuthority a = new SimpleGrantedAuthority("user");
        var auth = new UsernamePasswordAuthentication(username, null, List.of(a));

        /**
         * 보안 컨텍스트에 Authentication 객체를 저장한다.
         */
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request,response); // 다음 필터(다른 필터 or 권한부여 필터)를 호출한다.
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        /**
         * 로그인 요청에는 트리거 되지 않는다.
         */
        return request.getServletPath().equals("/login");
    }
}
