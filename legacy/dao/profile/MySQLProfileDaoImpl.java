package com.study.profile_stack_api.domain.profile.dao;

import com.study.profile_stack_api.domain.profile.entity.Position;
import com.study.profile_stack_api.domain.profile.entity.Profile;
import com.study.profile_stack_api.domain.profile.exception.ProfileNotFoundException;
import com.study.profile_stack_api.global.common.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL 기반 Profile DAO 구현
 */
@Repository
@RequiredArgsConstructor
public class MySQLProfileDaoImpl implements ProfileDao {
    public final JdbcTemplate jdbcTemplate;

    // ==================== CREATE ====================

    /**
     * 프로필 저장
     *
     * @param profile 저장할 프로필
     * @return 저장된 프로필 (ID 포함)
     */
    @Override
    public Profile save(Profile profile) {
        String sql = """
                INSERT INTO profile
                (name, email, bio, position, career_years, github_url, blog_url, member_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, profile.getName());
            ps.setString(2, profile.getEmail());
            ps.setString(3, profile.getBio());
            ps.setString(4, profile.getPosition().name());
            ps.setInt(5, profile.getCareerYears());
            ps.setString(6, profile.getGithubUrl());
            ps.setString(7, profile.getBlogUrl());
            ps.setLong(8, profile.getMemberId());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            profile.setId(generatedId.longValue());
        }

        return profile;
    }

    // ==================== READ ====================

    /**
     * 전체 프로필 조회 (최신순 정렬)
     *
     * @return 모든 프로필 리스트
     */
    @Override
    public List<Profile> findAll() {
        String sql = "SELECT * FROM profile ORDER BY created_at DESC, id DESC";
        return jdbcTemplate.query(sql, profileRowMapper);
    }

    /**
     * ID로 프로필 단건 조회
     *
     * @param id 조회할 프로필 ID
     * @return 프로필 (없다면 Null)
     */
    @Override
    public Optional<Profile> findById(Long id) {
        String sql = "SELECT * FROM profile WHERE id = ?";
        try {
            Profile profile = jdbcTemplate.queryForObject(sql, profileRowMapper, id);
            return Optional.ofNullable(profile);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 회원 ID로 프로필 단건 조회
     *
     * @param memberId 조회할 프로필 ID
     * @return 프로필 (없다면 Null)
     */
    @Override
    public Optional<Profile> findByMemberId(Long memberId) {
        String sql = "SELECT * FROM profile WHERE member_id = ?";
        try {
            Profile profile = jdbcTemplate.queryForObject(sql, profileRowMapper, memberId);
            return Optional.ofNullable(profile);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 직무로 프로필 조회
     *
     * @param position 조회할 직무
     * @return 직무별 프로필 리스트
     */
    @Override
    public List<Profile> findByPosition(Position position) {
        String sql = "SELECT * FROM profile WHERE position = ? ORDER BY created_at DESC, id DESC";
        return jdbcTemplate.query(sql, profileRowMapper, position.name());
    }

    // ==================== PAGING ====================

    /**
     * 전체 프로필 페이징 조회
     *
     * @param page 현재 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 모든 프로필 페이지
     */
    @Override
    public Page<Profile> findAllWithPaging(int page, int size) {
        // 전체 개수 조회
        String countSql = "SELECT COUNT(*) FROM profile";
        Long totalElements = jdbcTemplate.queryForObject(countSql, Long.class);

        // 전체 데이터가 없거나 0건이면 빈 페이지 반환
        if (totalElements == null || totalElements == 0) {
            return new Page<>(List.of(), page, size, 0);
        }

        // 해당 페이지 데이터 조회
        String dataSql = """
                SELECT * FROM profile
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """;

        int offset = page * size;
        List<Profile> content = jdbcTemplate.query(dataSql, profileRowMapper, size, offset);

        // Page 객체 생성 및 반환
        return new Page<>(content, page, size, totalElements);
    }

    /**
     * 직무별 프로필 페이징 조회
     *
     * @param positionName 조회할 직무
     * @param page 현재 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 직무별 프로필 페이지
     */
    @Override
    public Page<Profile> findByPositionWithPaging(String positionName, int page, int size) {
        // COUNT 쿼리에도 동일한 WHERE 조건 적용
        String countSql = "SELECT COUNT(*) FROM profile WHERE position = ?";
        Long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, positionName);

        // 전체 데이터가 없거나 0건이면 빈 페이지 반환
        if (totalElements == null || totalElements == 0) {
            return new Page<>(List.of(), page, size, 0);
        }

        // 해당 페이지 데이터 조회
        String dataSql = """
                SELECT * FROM profile
                WHERE position = ?
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """;

        int offset = page * size;
        List<Profile> content = jdbcTemplate.query(dataSql, profileRowMapper, positionName, size, offset);

        // Page 객체 생성 및 반환
        return new Page<>(content, page, size, totalElements);
    }

    /**
     * 검색 조건에 맞는 데이터를 페이지 단위로 조회
     *
     * @param nameKeyword 이름 검색어
     * @param position 직무 검색어
     * @param page 현재 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 검색 결과 페이지
     */
    @Override
    public Page<Profile> searchWithPaging(String nameKeyword, String position, int page, int size) {
        // 공통 WHERE 절 구성
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (nameKeyword != null && !nameKeyword.isBlank()) {
            whereClause.append(" AND name LIKE ?");
            params.add("%" + nameKeyword + "%");
        }

        if (position != null && !position.isBlank()) {
            whereClause.append(" AND position = ?");
            params.add(position);
        }

        // COUNT 쿼리 (WHERE 절 재사용)
        String countSql = "SELECT COUNT(*) FROM profile" + whereClause;
        Long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        // 전체 데이터가 없거나 0건이면 빈 페이지 반환
        if (totalElements == null || totalElements == 0) {
            return new Page<>(List.of(), page, size, 0);
        }

        // 데이터 쿼리 (WHERE 절 재상용 + 페이징)
        String dataSql = "SELECT * FROM profile"
                + whereClause
                + " ORDER BY created_at DESC, id DESC"
                + " LIMIT ? OFFSET ?";

        // 페이징 파라미터를 기존 파라미터에 추가
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(page * size);

        List<Profile> content = jdbcTemplate.query(dataSql, profileRowMapper, dataParams.toArray());

        // Page 객체 생성 및 반환
        return new Page<>(content, page, size, totalElements);
    }

    // ==================== UPDATE ====================

    /**
     * 프로필 수정
     */
    @Override
    public Profile update(Profile profile) {
        String sql = """
                UPDATE profile
                SET name = ?, email = ?, bio = ?, position = ?, career_years = ?, github_url = ?, blog_url = ?
                WHERE id = ?
                """;

        int updated = jdbcTemplate.update(sql,
                profile.getName(),
                profile.getEmail(),
                profile.getBio(),
                profile.getPosition().name(),
                profile.getCareerYears(),
                profile.getGithubUrl(),
                profile.getBlogUrl(),
                profile.getId());

        if (updated == 0) {
            throw new ProfileNotFoundException(profile.getId());
        }

        return profile;
    }

    // ==================== DELETE ====================

    /**
     * ID로 프로필 삭제
     *
     * @param id 삭제할 프로필 ID
     * @return 삭제 성공 여부 (true: 삭제됨, false: 해당 ID 없음)
     */
    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM profile WHERE id = ?";
        int deleted = jdbcTemplate.update(sql, id);
        return deleted > 0;
    }

    /**
     * 전체 프로필 삭제
     *
     * @return 삭제된 프로필 수
     */
    @Override
    public int deleteAll() {
        String sql = "DELETE FROM profile";
        return jdbcTemplate.update(sql);
    }

    // ==================== VALIDATION ====================

    /**
     * 이메일에 해당하는 프로필이 있는지 확인
     *
     * @param email 확인할 프로필 이메일
     * @return 존재여부
     */
    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM profile WHERE LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmailAndIdNot(Long id, String email) {
        String sql = "SELECT COUNT(*) FROM profile WHERE id != ? AND LOWER(email) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id, email);
        return count != null && count > 0;
    }

    /**
     * ID에 해당하는 프로필이 있는지 확인
     *
     * @param id 확인할 프로필 ID
     * @return 존재여부
     */
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM profile WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * ResultSet의 각 행을 Profile 객체로 변환
     */
    private final RowMapper<Profile> profileRowMapper = (rs, rowMapper) ->
            Profile.builder()
                    .id(rs.getLong("id"))
                    .memberId(rs.getLong("member_id"))
                    .name(rs.getString("name"))
                    .email(rs.getString("email"))
                    .bio(rs.getString("bio"))
                    .position(Position.valueOf(rs.getString("position")))
                    .careerYears(rs.getInt("career_years"))
                    .githubUrl(rs.getString("github_url"))
                    .blogUrl(rs.getString("blog_url"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build();
}
