package haru.harudrawer.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisService {

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenValidity; // 7일 (초 단위)

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     *  Refresh Token 저장 (기본 만료 시간 적용)
     */
    public void saveRefreshToken(String userEmail, String refreshToken) {
        String key = "refresh:" + userEmail; // Redis Key
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, refreshToken, refreshTokenValidity, TimeUnit.SECONDS);
    }

    /**
     *  Refresh Token 조회 (Optional 반환)
     */
    @Transactional(readOnly = true)
    public Optional<String> getRefreshToken(String userEmail) {
        String key = "refresh:" + userEmail;
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        String token = (String) values.get(key);
        return Optional.ofNullable(token);
    }

    /**
     *  Refresh Token 삭제 (로그아웃 시)
     */
    public void deleteRefreshToken(String userEmail) {
        String key = "refresh:" + userEmail;
        redisTemplate.delete(key);
    }

    /**
     *  특정 키의 만료 시간 변경
     */
    public void updateExpiration(String key, int timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }
}
