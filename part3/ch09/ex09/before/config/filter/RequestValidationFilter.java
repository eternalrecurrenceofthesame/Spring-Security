package springsecurity.ssia.ch9.previous.config.filter;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * BasicAuthenticationFilter(인증 필터) 앞에 추가되는 필터
 */
public class RequestValidationFilter implements Filter { // 필터 정의를 위한 Filer 인터페이스

    /**
     * @param servletRequest : HTTP 요청을 나타낸다. 요청에대한 세부 정보를 얻을 수 있다.
     * @param servletResponse : HTTP 응답을 나타낸다. 응답을 클라이언트로 다시 보내기 전 또는 더 나아가 필터 체인에서 응답을 변경한다.
     * @param filterChain : 필터 체인을 나타낸다. 체인의 다음 필터로 요청을 전달한다.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        var httpRequest = (HttpServletRequest) servletRequest;
        var httpResponse = (HttpServletResponse) servletResponse;

        String requestId = httpRequest.getHeader("Request-Id");
        if(requestId == null || requestId.isBlank()){
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return; // 헤더가 없으면 HTTP 상태가 400 으로 바뀌고 요청이 필터 체인 다음으로 전달되지 않는다.
        }

        filterChain.doFilter(servletRequest,servletResponse);
    }
}
