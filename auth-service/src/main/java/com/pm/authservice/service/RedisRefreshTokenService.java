package com.pm.authservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class RedisRefreshTokenService {

    private static final DefaultRedisScript<Long> ROTATE_REFRESH_TOKEN_SCRIPT;

    static {
        ROTATE_REFRESH_TOKEN_SCRIPT = new DefaultRedisScript<>();

        ROTATE_REFRESH_TOKEN_SCRIPT.setScriptText("""
                local storedUserId = redis.call('GET', KEYS[1])

                if not storedUserId then
                    return 0
                end

                if storedUserId ~= ARGV[1] then
                    return -1
                end

                redis.call('DEL', KEYS[1])
                redis.call('SREM', KEYS[2], ARGV[2])

                redis.call('SET', KEYS[3], ARGV[1], 'PX', ARGV[4])
                redis.call('SADD', KEYS[2], ARGV[3])
                redis.call('PEXPIRE', KEYS[2], ARGV[4])

                return 1
                """);

        ROTATE_REFRESH_TOKEN_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;

    public RedisRefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String tokenKey(String token) {
        return "refresh:" + token;
    }

    private String userKey(UUID userId) {
        return "user:" + userId + ":tokens";
    }

    public void store(String token, UUID userId, long expiryMillis) {
        Duration ttl = Duration.ofMillis(expiryMillis);

        redisTemplate.opsForValue()
                .set(tokenKey(token), userId.toString(), ttl);

        redisTemplate.opsForSet()
                .add(userKey(userId), token);

        redisTemplate.expire(userKey(userId), ttl);
    }

    public String getUserId(String token) {
        return redisTemplate.opsForValue().get(tokenKey(token));
    }

    /**
     * Atomically consumes the old refresh token and stores its replacement.
     *
     * @return true if rotation succeeded; false if the old token is no
     * longer active or does not belong to the expected user.
     */
    public boolean rotate(
            String oldToken,
            String newToken,
            UUID userId,
            long expiryMillis
    ) {
        if (expiryMillis <= 0) {
            throw new IllegalArgumentException(
                    "Refresh-token expiry must be positive"
            );
        }

        Long result = redisTemplate.execute(
                ROTATE_REFRESH_TOKEN_SCRIPT,
                List.of(
                        tokenKey(oldToken),
                        userKey(userId),
                        tokenKey(newToken)
                ),
                userId.toString(),
                oldToken,
                newToken,
                Long.toString(expiryMillis)
        );

        return Long.valueOf(1L).equals(result);
    }

    public void delete(String token) {
        String key = tokenKey(token);
        String userId = redisTemplate.opsForValue().get(key);

        if (userId != null) {
            redisTemplate.opsForSet()
                    .remove(userKey(UUID.fromString(userId)), token);
        }

        redisTemplate.delete(key);
    }

    public void deleteByUserId(UUID userId) {
        String key = userKey(userId);

        Set<String> tokens = redisTemplate.opsForSet().members(key);

        if (tokens != null && !tokens.isEmpty()) {
            for (String token : tokens) {
                redisTemplate.delete(tokenKey(token));
            }
        }

        redisTemplate.delete(key);
    }
}