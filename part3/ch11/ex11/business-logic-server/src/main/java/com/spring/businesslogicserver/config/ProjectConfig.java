package com.spring.businesslogicserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 비즈니스 논리 서버 내에서 사용하는 설정 정보들 (시큐리티 설정 정보와 구분한다.)
 */
@Configuration
public class ProjectConfig {
    /**
     * 인증 서버를 호출하기 위한 RestTemplate (RestService 에서 사용한다)
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
