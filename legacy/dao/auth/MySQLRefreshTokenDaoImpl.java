package com.study.profile_stack_api.domain.auth.dao;

import com.study.profile_stack_api.domain.auth.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MySQL 기반 Refresh Token DAO 구현
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MySQLRefreshTokenDaoImpl implements RefreshTokenDao {

    private final JdbcTemplate jdbcTemplate;

    // ==================== CREATE ====================

    /**
     * Refresh Token 저장
     */
    @Override
    public void saveRefreshToken(RefreshToken refreshToken) {
        String deleteSql = "DELETE FROM refresh_token WHERE member_id = ?";
        jdbcTemplate.update(deleteSql, refreshToken.getMemberId());

        String insertSql = "INSERT INTO refresh_token (member_id, token, expiry_date) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, refreshToken.getMemberId(), refreshToken.getToken(), refreshToken.getExpiryDate());
        log.debug("Saved refresh token for member ID: {}", refreshToken.getMemberId());
    }

    // ==================== READ ====================

    /**
     * 회원 ID로 Refresh Token 조회
     * @param memberId 회원 ID
     * @return Refresh Token (없다면 Null)
     */
    @Override
    public Optional<RefreshToken> findByMemberId(Long memberId) {
        String sql = "SELECT * FROM refresh_token WHERE member_id = ?";

        try {
            RefreshToken refreshToken = jdbcTemplate.queryForObject(sql, refreshTokenRowMapper, memberId);
            log.debug("Found refresh token by member ID: {}", memberId);
            return Optional.ofNullable(refreshToken);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No refresh token found with member ID: {}", memberId);
            return Optional.empty();
        }
    }

    // ==================== DELETE ====================z

    /**
     * Refresh Token 삭제
     */
    @Override
    public void deleteRefreshToken(String refreshToken) {
        String sql = "DELETE FROM refresh_token WHERE token = ?";
        jdbcTemplate.update(sql, refreshToken);
        log.debug("Deleted refresh token");
    }

    /**
     * 회원 ID로 Refresh Token 삭제
     */
    @Override
    public void deleteByMemberId(Long memberId) {
        String sql = "DELETE FROM refresh_token WHERE member_id = ?";
        jdbcTemplate.update(sql, memberId);
        log.debug("Deleted refresh token by member ID: {}", memberId);
    }

    /**
     * 만료된 모든 Refresh Token 삭제
     */
    @Override
    public int deleteExpiredRefreshTokens() {
        String sql = "DELETE FROM refresh_token WHERE expiry_date <= NOW()";
        int rowsAffected = jdbcTemplate.update(sql);
        log.debug("Deleted {} expired refresh tokens", rowsAffected);
        return rowsAffected;
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * ResultSet의 각 행을 Refresh Token 객체로 변환
     */
    private final RowMapper<RefreshToken> refreshTokenRowMapper = (rs, rowNum) ->
            RefreshToken.builder()
                    .id(rs.getLong("id"))
                    .memberId(rs.getLong("member_id"))
                    .token(rs.getString("token"))
                    .expiryDate(rs.getTimestamp("expiry_date").toLocalDateTime())
                    .build();
}
