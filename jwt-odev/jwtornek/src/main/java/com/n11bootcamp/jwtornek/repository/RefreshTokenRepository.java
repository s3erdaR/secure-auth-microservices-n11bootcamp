package com.n11bootcamp.jwtornek.repository;

import com.n11bootcamp.jwtornek.entity.RefreshToken;
import com.n11bootcamp.jwtornek.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}