package com.fmud.auth.application.port.out;

import com.fmud.auth.domain.model.User;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID id);

    List<User> findAll();

    boolean existsByEmail(String email);

    User save(User user);
}
