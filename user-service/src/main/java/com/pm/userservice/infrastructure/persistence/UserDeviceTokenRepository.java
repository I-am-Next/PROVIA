package com.pm.userservice.infrastructure.persistence;

import com.pm.userservice.domain.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, UUID> {

    List<UserDeviceToken> findByUserId(UUID userId);

    Optional<UserDeviceToken> findByDeviceToken(String token);

    void deleteByLastUsedAtBefore(Instant cutoff);
}
