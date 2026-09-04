package com.fmud.auth.infrastructure.adapter.out.persistence;

import com.fmud.auth.application.port.out.UserRepositoryPort;
import com.fmud.auth.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Repository
public class UserPersistenceAdapter implements UserRepositoryPort {
    private final SpringDataUserRepository repository;

    public UserPersistenceAdapter(SpringDataUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .sorted(Comparator.comparing(User::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        return toDomain(repository.save(toEntity(user)));
    }

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setName(user.name());
        entity.setEmail(user.email());
        entity.setPasswordHash(user.passwordHash());
        entity.setRole(user.role());
        entity.setEnabled(user.enabled());
        entity.setCreatedAt(user.createdAt());
        entity.setUpdatedAt(user.updatedAt());
        return entity;
    }
}
