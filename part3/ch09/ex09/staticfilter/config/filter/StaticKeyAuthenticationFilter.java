package springsecurity.ssia.ch9.staticfilter.config.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@Component
public class StaticKeyAuthenticationFilter implements Filter {

    /**
     * 간단한 실습을 위해 데이터베이스가 아닌 설정 파일에 지정된 정적 키 값을 꺼낸다.
     */
    @Value("${authorization.key}")
    private String authorizationKey;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var httpRequest = (HttpServletRequest) servletRequest;
        var httpResponse = (HttpServletResponse) servletResponse;

        String authentication = httpRequest.getHeader("Authorization");

        if(authorizationKey.equals(authentication)){
            filterChain.doFilter(servletRequest,servletResponse); // 인증이 확인되면 다음 필터 호출
        }else{
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
