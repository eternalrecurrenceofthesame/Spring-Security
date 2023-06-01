package springsecurity.ssia.ch3.ex.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class DummyUser implements UserDetails {

    /**
     * final 로 만들어서 User 는 불변 객체가 된다
     *
     * 예제를 간단하게 하기 위해 권한은 하나만 부여한다.
     */

    private final String username;
    private final String password;
    private final String authority;

    public DummyUser(String username, String password, String authority) {
        this.username = username;
        this.password = password;
        this.authority = authority;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(()-> authority); // 지정한 이름의 권한을 리스트로 만들어서 반환
    }


    /**
     * 계정이 만료되거나 잠금되지 않게 모두 true 로 고정했다.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
