package springsecurity.ssia.ch10.ex1.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;

import javax.servlet.*;
import java.io.IOException;
import java.util.logging.Logger;

@Slf4j
public class CsrfTokenLogger implements Filter {

    private Logger logger = Logger.getLogger(CsrfTokenLogger.class.getName());

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Object o = servletRequest.getAttribute("_csrf");
        CsrfToken token = (CsrfToken) o;

        log.info("CSRF token" + token.getToken());

        filterChain.doFilter(servletRequest,servletResponse);
    }
}
