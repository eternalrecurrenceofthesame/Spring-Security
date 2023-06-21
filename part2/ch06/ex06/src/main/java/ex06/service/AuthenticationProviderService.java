package ex06.service;

import ex06.model.CustomUserDetails;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * 커스텀 인증 공급자를 구현한다. 인증 공급자는 UserDetailsService 로 UserDetails 를 조회한다.
 * 조회한 유저 정보에 맞는 인코더를 사용해서 사용자 인증 요청 비밀번호를 인코딩하고 데이터베이스에
 * 저장된 데이터의 비밀번호와 일치하는지 비교한다.
 */
@Service
public class AuthenticationProviderService implements AuthenticationProvider {

    private JpaUserDetailsService userDetailsService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private SCryptPasswordEncoder sCryptPasswordEncoder;

    public AuthenticationProviderService(JpaUserDetailsService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder, SCryptPasswordEncoder sCryptPasswordEncoder) {
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sCryptPasswordEncoder = sCryptPasswordEncoder;
    }

    /**
     * 인증 공급자가 사용하는 Authentication 은 Request 요청을 받는 AuthenticationFilter 에서 생성해준다.
     * formLogin 을 사용할 경우 UsernamePasswordAuthenticationFilter 가 적용된다.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        CustomUserDetails user = userDetailsService.loadUserByUsername(username);

        /**
         * 유저 엔티티에 저장된 알고리즘에 따라서 패스워드 인코더를 적용한다.
         */
        switch(user.getUser().getAlgorithm()){

            case BCRYPT:
                return checkPassword(user, password, bCryptPasswordEncoder);
            case SCRYPT:
                return checkPassword(user, password, sCryptPasswordEncoder);
        }

        throw new BadCredentialsException("Bad credentials");

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Authentication checkPassword(CustomUserDetails user, String rawPassword, PasswordEncoder encoder){

        if(encoder.matches(rawPassword, user.getPassword())){
            return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
        }else{
            throw new BadCredentialsException("Bad credentials");
        }
    }
}
