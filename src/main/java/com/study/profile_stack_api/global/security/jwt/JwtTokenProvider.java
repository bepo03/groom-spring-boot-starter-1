package com.study.profile_stack_api.global.security.jwt;

import com.study.profile_stack_api.domain.auth.entity.RefreshToken;
import com.study.profile_stack_api.domain.auth.exception.ExpiredTokenException;
import com.study.profile_stack_api.domain.auth.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT 생성, 검증, 파싱을 담당하는 유틸리티 클래스
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}")
            String secret,
            @Value("${jwt.access-token-expiration}")
            long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}")
            long refreshTokenExpiration
    ) {
        // 설정된 secret 문자열로 부터 HMAC-SHA 서명용 SecretKey 생성
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * username과 role을 기반으로 Access Token 생성
     */
    public String createAccessToken(String username, String roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * username을 기반으로 Refresh Token 생성
     * Refresh Token에는 role 정보를 포함하지 않음
     */
    public RefreshToken createRefreshToken(Long memberId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);
        String token =  Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        return RefreshToken.builder()
                .member_id(memberId)
                .token(token)
                .expiry_date(LocalDateTime.ofInstant(expiryDate.toInstant(), ZoneId.systemDefault()))
                .build();
    }

    /**
     * 토큰에서 username 추출
     */
    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            throw new InvalidTokenException("인증 토큰이 유효하지 않습니다.");
        }
    }

    /**
     * 토큰 유효성 검증
     * 서명이 올바른지, 만료되지 않았는지 등을 확인
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token has expired", e);
            throw new ExpiredTokenException("인증 토큰이 만료되었습니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e);
            throw new InvalidTokenException("지원되지 않는 인증 토큰입니다.");
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token format", e);
            throw new InvalidTokenException("인증 토큰 형식이 올바르지 않습니다.");
        } catch (SecurityException e) {
            log.error("Invalid JWT signature", e);
            throw new InvalidTokenException("인증 토큰 서명이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty", e);
            throw new InvalidTokenException("JWT 클레임이 비어 있습니다.");
        }
    }

    /**
     * 토큰이 리프레시 토큰인지 확인
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = claims.get("type", String.class);
            return "refresh".equals(type);
        } catch (Exception e) {
            log.error("Failed to check token type", e);
            return false;
        }
    }

    /**
     * 토큰 만료 시간 가져오기
     */
    public Date getExpirationFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Failed to extract expiration from token", e);
            throw new InvalidTokenException("잘못된 토큰입니다.");
        }
    }
}
