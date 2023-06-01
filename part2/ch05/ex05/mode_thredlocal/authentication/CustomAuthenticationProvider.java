package springsecurity.ssia.ch5.mode_thredlocal.authentication;

import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@AllArgsConstructor
//@Component
public class CustomAuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

    private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> authenticationType) {
        return authenticationType.equals(UsernamePasswordAuthenticationToken.class);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        /**
         * 사용자가 없으면 UsernameNotFoundException 을 던진다
         */
        UserDetails u = userDetailsService.loadUserByUsername(username);

        if(passwordEncoder.matches(password, u.getPassword())){
            return new UsernamePasswordAuthenticationToken(
                    username,
                    password,
                    u.getAuthorities()); // 암호가 일치하면 필요한 세부 정보가 포함된 Authentication 계약 반환
        }else{
            /**
             * BadCredentialsException 은 AuthenticationException 을 상속한다
             * 오류가 던져지면 인증 프로세스가 중단되고 HTTP 필터는 응답 상태를 HTTP 401 권한 없음으로 설정한다.
             */
            throw new BadCredentialsException("무언가 잘못 됐습니다!");
        }
    }


}
