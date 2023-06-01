package springsecurity.ssia.ch9.after.config.filter;

import org.springframework.web.filter.OncePerRequestFilter;
import springsecurity.ssia.ch9.after.config.filter.AuthenticationLoggingFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class OnceAuthenticationLoggingFilter extends OncePerRequestFilter {

    private final Logger logger = Logger.getLogger(AuthenticationLoggingFilter.class.getName());


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestId = request.getHeader("Request-Id");

        logger.info("인증에 성공했습니다! 요청 아이디: " + requestId);

        filterChain.doFilter(request,response);
    }
}
