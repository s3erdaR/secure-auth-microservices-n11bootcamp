package com.n11bootcamp.jwtornek.repository;

import com.n11bootcamp.jwtornek.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}