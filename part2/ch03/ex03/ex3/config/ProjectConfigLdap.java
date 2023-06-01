package springsecurity.ssia.ch3.ex3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.DefaultLdapUsernameToDnMapper;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;

@Configuration
public class ProjectConfigLdap {

    @Bean
    public UserDetailsService userDetailsService(){
        var cs = new DefaultSpringSecurityContextSource( // 컨텍스트 소스를 생성해서 LDAP 서버의 주소 지정
                "ldap://127.0.0.1:33389/dc=springframework,dc=org");
        cs.afterPropertiesSet();

        var manager = new LdapUserDetailsManager(cs); // 매니저 인스턴스 생성

        manager.setUsernameMapper( // 사용자 이름 매퍼를 설정해 매니저에 사용자를 검색하는 방법을 지시한다
                new DefaultLdapUsernameToDnMapper("ou=groups","uid"));

        manager.setGroupSearchBase("ou=groups"); // 앱이 사용자를 검색하는 데 필요한 그룹 검색 기준 설정

        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
}
