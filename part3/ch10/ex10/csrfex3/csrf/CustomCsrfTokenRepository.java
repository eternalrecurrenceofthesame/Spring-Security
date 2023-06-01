package com.securty.csrfex3.csrf;

import com.securty.csrfex3.entity.Token;
import com.securty.csrfex3.repository.JpaTokenRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;


/**
 * JpaTokenRepository 에 접근하는 토큰 리포지토리! 파사드의 일종이 된다.
 * CSRF 토큰을 데이터베이스에 얻거나 저장하는 역할을 한다.
 */
public class CustomCsrfTokenRepository implements CsrfTokenRepository {

    @Autowired
    private JpaTokenRepository jpaTokenRepository;

    public CustomCsrfTokenRepository() {}

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        String uuid = UUID.randomUUID().toString();
        return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", uuid);
    }

    @Override
    public void saveToken(CsrfToken csrfToken, HttpServletRequest request, HttpServletResponse response) {

        String identifier = request.getHeader("X-IDENTIFIER");
        Optional<Token> existingToken = jpaTokenRepository.findByIdentifier(identifier);

        if(existingToken.isPresent()){
            Token token = existingToken.get();
            token.setToken(csrfToken.getToken());
        }else{
            Token token = new Token();
            token.setToken(csrfToken.getToken());
            token.setIdentifier(identifier); // 세션
            jpaTokenRepository.save(token);
        }
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        String identifier = request.getHeader("X-IDENTIFIER");
        Optional<Token> existingToken = jpaTokenRepository.findByIdentifier(identifier);

        if (existingToken.isPresent()) {
            Token token = existingToken.get();
            return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token.getToken());
        }

        return null;
    }


}
