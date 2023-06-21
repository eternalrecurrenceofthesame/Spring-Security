package ex06.model;

import ex06.entity.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 인증 공급자가 인증 논리에 사용할 유저 상세 정보 계약을 구현한 클래스
 * 유저 상세 정보는 UserDetailsService 로 호출할 수 있다. (사용자 이름으로 데이터베이스에 저장된 사용자를 검색)
 *
 * 시큐리티 공급자가 사용할 유저 상세 정보 계약과 데이터베이스에 저장되는 User 엔티티 정보를 분리해서 구현하면
 * 두 가지 책임이 혼합되지 않고 애플리케이션의 유지 보수성을 높일 수 있다.
 */

public class CustomUserDetails implements UserDetails {

    private final User user; // 데이터베이스에 저장되는 사용자 엔티티

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities().stream()
                .map(a -> new SimpleGrantedAuthority(a.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

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

    public final User getUser(){
        return user;
    }
}
