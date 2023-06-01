package springsecurity.ssia.ch5.html_auth.handlers;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * 인증 성공 시나리오 논리 구현
 */
@Component
public class CustomAuthenticationSuccessHandler  implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        var authorities = authentication.getAuthorities();

        var auth = authorities.stream()
                .filter(a -> a.getAuthority().equals("write"))
                .findFirst(); // 없으면 null 반환

        if(auth.isPresent()){
            httpServletResponse.sendRedirect("/home");
        }else{
            httpServletResponse.setStatus(SC_UNAUTHORIZED);
        }
    }


}
