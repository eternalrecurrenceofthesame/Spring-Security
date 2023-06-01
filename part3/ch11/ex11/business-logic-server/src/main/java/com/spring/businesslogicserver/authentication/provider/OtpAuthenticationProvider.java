package com.spring.businesslogicserver.authentication.provider;

import com.spring.businesslogicserver.authentication.OtpAuthentication;
import com.spring.businesslogicserver.authentication.proxy.AuthenticationServerProxy;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class OtpAuthenticationProvider implements AuthenticationProvider {

    private AuthenticationServerProxy proxy;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String code = String.valueOf(authentication.getCredentials());

        boolean result = proxy.sendOTP(username, code);

        if(result){
            return new OtpAuthentication(username, code);
        }else{
            throw new BadCredentialsException("잘못된 OTP 입니다.");
        }

    }

    @Override
    public boolean supports(Class<?> aClass) {
        return OtpAuthentication.class.isAssignableFrom(aClass);
    }
}
