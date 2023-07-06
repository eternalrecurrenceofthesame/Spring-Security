package springsecurity.ssia.ch3.ex3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

//@Configuration
public class ProjectConfig {

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource){

        // Jdbc 를 사용해서 값을 가져올 때 필요한 것들 테이블 컬럼 이름이 바뀌더라도 
        // 아래 형식에 맞춰서 쿼리를 생성하면 된다. 
        String usersByUsernameQuery =
                "select username, password, enabled from spring_users where username = ?";
        String authsByUserQuery =
                "select username, authority, from spring_authorities where username = ?";


        var userDetailsManager = new JdbcUserDetailsManager(dataSource);
        userDetailsManager.setUsersByUsernameQuery(usersByUsernameQuery);
        userDetailsManager.setAuthoritiesByUsernameQuery(authsByUserQuery);

        return userDetailsManager;
    }


    /**
     * 학습 편의를 위해 NoOpPasswordEncoder 를 사용한다
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return NoOpPasswordEncoder.getInstance();
    }
}
