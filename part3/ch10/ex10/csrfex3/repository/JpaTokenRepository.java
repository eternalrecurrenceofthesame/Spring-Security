package com.securty.csrfex3.repository;

import com.securty.csrfex3.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaTokenRepository extends JpaRepository<Token, Integer> {

    Optional<Token> findByIdentifier(String identifier);
}
