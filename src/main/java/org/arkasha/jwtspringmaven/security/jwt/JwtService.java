package org.arkasha.jwtspringmaven.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.arkasha.jwtspringmaven.dto.JwtAuthenticationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class JwtService {

    private static final Logger LOGGER = LogManager.getLogger(JwtService.class);

    @Value("${jwt-secret}")
    private String jwtSecret;

    // Черный список токенов
    private final ConcurrentMap<String, Long> tokenBlacklist = new ConcurrentHashMap<>();
    private static final long BLACKLIST_CLEANUP_DELAY = 30 * 60 * 1000; // 30 минут в миллисекундах

    public JwtAuthenticationDto generateAuthToken(String email) {
        JwtAuthenticationDto jwtAuthenticationDto = new JwtAuthenticationDto();
        jwtAuthenticationDto.setToken(generateJwtToken(email));
        jwtAuthenticationDto.setRefreshToken(generateRefreshToken(email));
        return jwtAuthenticationDto;
    }

    public JwtAuthenticationDto refreshBaseToken(
            String email,
            String oldRefreshToken,
            String oldToken) {
        if (oldRefreshToken != null && !oldRefreshToken.isEmpty()) {
            addToBlacklist(oldRefreshToken);
        }
        if (oldToken != null && !oldToken.isEmpty()) {
            addToBlacklist(oldToken);
        }

        JwtAuthenticationDto jwtAuthenticationDto = new JwtAuthenticationDto();
        jwtAuthenticationDto.setToken(generateJwtToken(email));
        jwtAuthenticationDto.setRefreshToken(generateRefreshToken(email));
        return jwtAuthenticationDto;
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSingInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateJwtToken(String token) {
        try {
            // Проверяем, находится ли токен в черном списке
            if (isTokenBlacklisted(token)) {
                LOGGER.warn("Token is blacklisted");
                return false;
            }

            Jwts.parser()
                    .verifyWith(getSingInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException expEx) {
            LOGGER.error("Expired JwtException", expEx);
            // Добавляем просроченный токен в черный список
            addToBlacklist(token);
        } catch (UnsupportedJwtException expEx) {
            LOGGER.error("Unsupported JwtException", expEx);
        } catch (MalformedJwtException expEx) {
            LOGGER.error("Malformed JwtException", expEx);
        } catch (SecurityException expEx) {
            LOGGER.error("Security Exception", expEx);
        } catch (Exception expEx) {
            LOGGER.error("invalid token", expEx);
        }
        return false;
    }

    /**
     * Добавляет токен в черный список
     */
    private void addToBlacklist(String token) {
        tokenBlacklist.put(token, System.currentTimeMillis());
        LOGGER.debug("Token added to blacklist. Current blacklist size: {}", tokenBlacklist.size());
    }

    private boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.containsKey(token);
    }

    @Scheduled(fixedRate = BLACKLIST_CLEANUP_DELAY)
    private void cleanupBlacklist() {
        long currentTime = System.currentTimeMillis();
        long cleanupThreshold = currentTime - BLACKLIST_CLEANUP_DELAY;

        int initialSize = tokenBlacklist.size();

        tokenBlacklist.entrySet().removeIf(entry ->
                entry.getValue() < cleanupThreshold
        );

        int removedCount = initialSize - tokenBlacklist.size();
        if (removedCount > 0) {
            LOGGER.info("Cleaned up {} tokens from blacklist. Current size: {}",
                    removedCount, tokenBlacklist.size());
        }
    }

    public String generateJwtToken(String email) {
        return generateToken(email, Duration.ofMinutes(5));
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, Duration.ofDays(1));
    }

    private SecretKey getSingInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(String email, Duration validity) {
        Date expirationDate = Date.from(
                LocalDateTime.now()
                        .plus(validity)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );

        return Jwts.builder()
                .subject(email)
                .expiration(expirationDate)
                .signWith(getSingInKey())
                .compact();
    }

}