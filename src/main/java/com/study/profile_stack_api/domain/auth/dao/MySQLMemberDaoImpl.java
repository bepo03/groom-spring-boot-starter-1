package com.study.profile_stack_api.domain.auth.dao;

import com.study.profile_stack_api.domain.auth.entity.Member;
import com.study.profile_stack_api.domain.auth.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

/**
 * MySQL 기반 Member DAO 구현
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MySQLMemberDaoImpl implements MemberDao {

    private final JdbcTemplate jdbcTemplate;

    // ==================== CREATE ====================

    /**
     * 회원 저장
     *
     * @param member 저장할 회원
     * @return 저장된 회원
     */
    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO member (username, passsword, role, enabled) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, member.getUsername());
            ps.setString(2, member.getPassword());

            Role role = member.getRole() != null ? member.getRole() : Role.USER;
            ps.setString(3, role.name());

            ps.setBoolean(4, member.isEnabled());

            return ps;
        }, keyHolder);

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        log.info("Created new member with ID: {}", generatedId);

        return findById(generatedId).orElseThrow(
                () -> new RuntimeException("Failed to retrieve created member")
        );
    }

    // ==================== READ ====================

    /**
     * ID로 회원 단건 조회
     *
     * @param id 조회할 회원 ID
     * @return 회원 (없다면 Null)
     */
    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";

        try {
            Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, id);
            log.debug("Found member by ID: {}", id);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No member found with ID: {}", id);
            return Optional.empty();
        }
    }

    /**
     * 이름으로 회원 단건 조회
     *
     * @param username 조회할 회원 이름
     * @return 회원 (없다면 Null)
     */
    @Override
    public Optional<Member> findByUsername(String username) {
        String sql = "SELECT * FROM member WHERE username = ?";

        try {
            Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, username);
            log.debug("Found member by username: {}", username);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No member found with username: {}", username);
            return Optional.empty();
        }
    }

    // ==================== UPDATE ====================

    /**
     * 회원 정보 수정
     */
    @Override
    public int update(Member member) {
        String sql = "UPDATE member SET username = ?, password = ?, role = ?, enabled = ? WHERE id = ?";

        String roleStr = member.getRole() != null ? member.getRole().name() : "USER";

        int rowsAffected = jdbcTemplate.update(sql,
                member.getUsername(),
                member.getPassword(),
                roleStr,
                member.isEnabled(),
                member.getId()
        );

        log.debug("Updated member with ID: {}, rows affected: {}", member.getId(), rowsAffected);
        return rowsAffected;
    }

    // ==================== DELETE ====================

    /**
     * ID로 회원 삭제
     */
    @Override
    public int deleteById(Long id) {
        String sql = "DELETE FROM member WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql, id);
        log.debug("Deleted member with ID: {}, rows affected: {}", id, rowsAffected);

        return rowsAffected;
    }

    // ==================== VALIDATION ====================

    /**
     * 이름에 해당하는 회원이 있는지 확인
     *
     * @param username 확인할 회원 이름
     * @return 존재여부
     */
    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM member WHERE username = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);

        return count != null && count > 0;
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * ResultSet의 각 행을 Member 객체로 변환
     */
    private final RowMapper<Member> memberRowMapper = (rs, rowNum) -> {
        String roleStr = rs.getString("role");
        Role role = Role.USER;

        if (roleStr != null) {
            try {
                role = Role.valueOf(roleStr);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role value: {}, using default USER", roleStr);
            }
        }

        return Member.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .role(role)
                .enabled(rs.getBoolean("enabled"))
                .createdAt(rs.getTimestamp("createdAt").toLocalDateTime())
                .build();
    };
}
