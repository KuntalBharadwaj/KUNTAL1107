package com.cg.cred_metric.repositories;

import com.cg.cred_metric.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRespository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
