package ex06.service;

import ex06.entity.User;
import ex06.model.CustomUserDetails;
import ex06.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

/**
 * Jpa 를 사용하는 UserDetailsService 를 구현한다.
 *
 * UserDetailsService 는 사용자의 이름으로 사용자를 검색하는 기능을 제공한다면 이 인터페이스를 확장한
 * UserDetailsManager 는 사용자의 추가 수정 삭제 기능을 구현할 수 있다.
 *
 * 여기서는 UserDetailsService 를 구현해서 단순 조회 기능을 만든다.
 */

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 유저 이름으로 인증을 위한 유저를 조회한다. 조회한 유저는 유저 상세 정보로 반환한다.
     */
    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        /**
         * Supplier 는 매개 변수를 받지 않고 단순히 무언가를 반환하는 추상 메서드이다.
         */
        Supplier<UsernameNotFoundException> s =
                () -> new UsernameNotFoundException("Problem during authentication !");

        User u = userRepository.findUserByUsername(username).orElseThrow(s);

        // 유저 상세정보를 반환한다.
        return new CustomUserDetails(u);
    }
}
