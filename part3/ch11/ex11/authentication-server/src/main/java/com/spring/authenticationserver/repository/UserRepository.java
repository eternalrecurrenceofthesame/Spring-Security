package com.spring.authenticationserver.repository;

import com.spring.authenticationserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 애노테이션을 안 적어주면 주입이 안 되는 경우가 있어서 명시함.
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);
}
