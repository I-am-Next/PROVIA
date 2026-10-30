package com.pm.userservice.infrastructure.persistence;

import com.pm.userservice.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByAuthUserId(String authUserId);

    @Modifying
    @Query(
            value = """
                    INSERT INTO user_profiles (
                        auth_user_id,
                        email,
                        created_at
                    )
                    VALUES (
                        :authUserId,
                        :email,
                        CURRENT_TIMESTAMP
                    )
                    ON CONFLICT (auth_user_id)
                    DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("authUserId") String authUserId,
            @Param("email") String email
    );
}