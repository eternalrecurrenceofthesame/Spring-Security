package springsecurity.ssia.ch9.after.config.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * 인증 성공후 이벤트를 저장하는 필터
 */
public class AuthenticationLoggingFilter implements Filter {

    private final Logger logger = Logger.getLogger(AuthenticationLoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) servletRequest;

        var requestId = httpRequest.getHeader("Request-Id");

        logger.info("인증에 성공했습니다! 요청 아이디: " + requestId); // 헤더에서 아이디를 얻고 로그로 기록

        filterChain.doFilter(servletRequest, servletResponse); // 요청을 필터 체인의 다음 필터에 전달.
    }
}
