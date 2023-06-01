package springsecurity.ssia.ch2.ex.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = String.valueOf(authentication.getCredentials());

        // user ,12345 의 요청만 승인된다.
        //UserDetailsService, PasswordEncoder 를 호출해서 사용자 이름과 암호 테스트 ??
        if("user".equals(username) && "12345".equals(password)){
            return new UsernamePasswordAuthenticationToken(username, password, Arrays.asList());
        }else{
            throw new AuthenticationCredentialsNotFoundException("오류!");
        }
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
