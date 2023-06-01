package com.spring.securitytest.test.testconfig;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class CustomSecurityContextFactory implements WithSecurityContextFactory<WithCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithCustomUser withCustomerUser) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // 맞춤형 Authentication 제작
        var a = new UsernamePasswordAuthenticationToken(withCustomerUser.username(), null, null);
        context.setAuthentication(a);

        return context;
    }
}
