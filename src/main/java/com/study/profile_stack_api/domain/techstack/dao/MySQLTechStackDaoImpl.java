package com.study.profile_stack_api.domain.techstack.dao;

import com.study.profile_stack_api.domain.techstack.entity.Proficiency;
import com.study.profile_stack_api.domain.techstack.entity.TechCategory;
import com.study.profile_stack_api.domain.techstack.entity.TechStack;
import com.study.profile_stack_api.global.common.Page;
import com.study.profile_stack_api.domain.profile.exception.ProfileNotFoundException;
import com.study.profile_stack_api.domain.techstack.exception.TechStackNotFoundException;
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
 * MySQL 기반 TechStack DAO 구현
 */
@Repository
public class MySQLTechStackDaoImpl implements TechStackDao {
    public final JdbcTemplate jdbcTemplate;

    public MySQLTechStackDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ==================== CREATE ====================

    /**
     * 프로필별 기술 스택 저장
     *
     * @param techStack 저장할 기술 스택
     * @return 저장된 기술 스택 (ID 포함)
     */
    @Override
    public TechStack saveByProfileId(Long profileId, TechStack techStack) {
        if (!existsById(profileId)) {
            throw new ProfileNotFoundException(profileId);
        }

        String sql = """
                INSERT INTO tech_stack (profile_id, name, category, proficiency, years_of_exp)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, profileId);
            ps.setString(2, techStack.getName());
            ps.setString(3, techStack.getCategory().name());
            ps.setString(4, techStack.getProficiency().name());
            ps.setInt(5, techStack.getYearsOfExp());
            return ps;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();
        if (generatedId != null) {
            techStack.setId(generatedId.longValue());
        }

        return techStack;
    }

    // ==================== READ ====================

    /**
     * 프로필별 기술 스택 전체 조회 (최신순 정렬)
     *
     * @return 프로필별 기술 스택 전체 리스트
     */
    @Override
    public List<TechStack> findAllByProfileId(Long profileId) {
        String sql = "SELECT * FROM tech_stack WHERE profile_id = ? ORDER BY created_at DESC, id DESC";
        return jdbcTemplate.query(sql, techStackRowMapper, profileId);
    }

    /**
     * 프로필별 기술 스택 ID로 단건 조회
     *
     * @param id 조회할 기술 스택 ID
     * @return 기술 스택 (없다면 Null)
     */
    @Override
    public Optional<TechStack> findByProfileIdAndId(Long profileId, Long id) {
        String sql = "SELECT *FROM tech_stack WHERE profile_id = ? AND id = ?";

        try {
            TechStack techStack = jdbcTemplate.queryForObject(sql, techStackRowMapper, profileId, id);
            return Optional.ofNullable(techStack);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // ==================== PAGING ====================

    /**
     * 프로필별 전체 기술 스택 페이징 조회
     *
     * @param page 현재 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 모든 프로필 페이지
     */
    @Override
    public Page<TechStack> findAllWithPaging(Long profileId, int page, int size) {
        // 전체 개수 조회
        String countSql = "SELECT COUNT(*) FROM tech_stack WHERE profile_id = ?";
        Long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, profileId);

        // 전체 데이터가 없거나 0건이면 빈 페이지 반환
        if (totalElements == null || totalElements == 0) {
            return new Page<>(List.of(), page, size, 0);
        }

        // 해당 페이지 데이터 조회
        String dataSql = """
                SELECT * FROM tech_stack
                WHERE profile_id = ?
                ORDER BY created_at DESC, id DESC
                LIMIT ? OFFSET ?
                """;

        int offset = page * size;
        List<TechStack> content = jdbcTemplate.query(dataSql, techStackRowMapper, profileId, size, offset);

        // Page 객체 생성 및 반환
        return new Page<>(content, page, size, totalElements);
    }

    /**
     * 프로필별 기술 카테고리로 기술 스택 페이징 조회
     *
     * @param categoryName 조회할 기술 카테고리
     * @param page 현재 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 기술 카테고리별 기술 스택 페이지
     */
    @Override
    public Page<TechStack> findByCategoryWithPaging(Long profileId, String categoryName, int page, int size) {
        // COUNT 쿼리에도 동일한 WHERE 조건 적용
        String countSql = "SELECT COUNT(*) FROM tech_stack WHERE profile_id = ? AND category = ?";
        Long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, profileId, categoryName);

        // 전체 데이터가 없거나 0건이면 빈 페이지 반환
        if (totalElements == null || totalElements == 0) {
            return new Page<>(List.of(), page, size, 0);
        }

        // 해당 페이지 데이터 조회
        String dataSql = """
                SELECT * FROM tech_stack
                WHERE profile_id = ? AND category = ?
                ORDER BY created_at, id DESC
                LIMIT ? OFFSET ?
                """;

        int offset = page * size;
        List<TechStack> content = jdbcTemplate.query(dataSql, techStackRowMapper, profileId, categoryName, size, offset);

        // Page 객체 생성 및 반환
        return new Page<>(content, page, size, totalElements);
    }

    /**
     * 검색 조건에 맞는 데이터를 페이지 단위로 조회
     *
     * @param categoryKeyWord 카테고리 검색어
     * @param proficiencyKeyword 숙련도 검색어
     * @param page 현재 페이지 번호
     * @param size 페이지당 데이터 개수
     * @return 검색 결과 페이지
     */
    @Override
    public Page<TechStack> searchWithPaging(
            Long profileId, String categoryKeyWord, String proficiencyKeyword, int page, int size
    ) {
        // 공통 WHERE 절 구성
        StringBuilder whereClause = new StringBuilder(" WHERE profile_id = ? AND 1=1");
        List<Object> params = new ArrayList<>();
        params.add(profileId);

        if (categoryKeyWord != null && !categoryKeyWord.isBlank()) {
            whereClause.append(" AND category = ?");
            params.add(categoryKeyWord);
        }

        if (proficiencyKeyword != null && !proficiencyKeyword.isBlank()) {
            whereClause.append(" AND proficiency = ?");
            params.add(proficiencyKeyword);
        }

        // COUNT 쿼리 (WHERE 절 재사용)
        String countSql = "SELECT COUNT(*) FROM tech_stack" + whereClause;
        Long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        // 전체 데이터가 없거나 0건이면 빈 페이지 반환
        if (totalElements == null || totalElements == 0) {
            return new Page<>(List.of(), page, size, 0);
        }

        // 데이터 쿼리 (WHERE 절 재상용 + 페이징)
        String dataSql = "SELECT * FROM tech_stack"
                + whereClause
                + " ORDER BY created_at DESC, id DESC"
                + " LIMIT ? OFFSET ?";

        // 페이징 파라미터를 기존 파라미터에 추가
        List<Object> dataParams = new ArrayList<>(params);
        dataParams.add(size);
        dataParams.add(page * size);

        List<TechStack> content = jdbcTemplate.query(dataSql, techStackRowMapper, dataParams.toArray());

        // Page 객체 생성 및 반환
        return new Page<>(content, page, size, totalElements);
    }

    // ==================== UPDATE ====================

    /**
     * 프로필별 기술 스택 수정
     *
     * @param techStack 수정할 기술 스택
     * @return 기술 스택
     */
    @Override
    public TechStack updateByProfileId(Long profileId, TechStack techStack) {
        String sql = """
                UPDATE tech_stack
                SET name = ?, category = ?, proficiency = ?, years_of_exp = ?
                WHERE profile_id = ? AND id = ?
                """;

        int updated = jdbcTemplate.update(sql,
                techStack.getName(),
                techStack.getCategory().name(),
                techStack.getProficiency().name(),
                techStack.getYearsOfExp(),
                profileId,
                techStack.getId());

        if (updated == 0) {
            throw new TechStackNotFoundException(techStack.getId());
        }

        return techStack;
    }

    // ==================== DELETE ====================

    /**
     * 프로필별 기술 스택 ID로 단건 삭제
     *
     * @param id 삭제할 기술 스택 ID
     * @return 삭제 성공 여부 (true: 삭제됨, false: 해당 ID 없음)
     */
    @Override
    public boolean deleteByProfileIdAndId(Long profileId, Long id) {
        String sql = "DELETE FROM tech_stack WHERE profile_id = ? AND id = ?";
        int deleted = jdbcTemplate.update(sql, profileId, id);
        return deleted > 0;
    }

    /**
     * 프로필별 기술 스택 전체 삭제
     *
     * @return 삭제된 기술 스택 수
     */
    @Override
    public int deleteAllByProfileId(Long profileId) {
        String sql = "DELETE FROM tech_stack WHERE profile_id = ?";
        return jdbcTemplate.update(sql, profileId);
    }

    // ==================== VALIDATION ====================

    /**
     * ID에 해당하는 기술 스택이 있는지 확인
     *
     * @param id 확인할 기술 스택 ID
     * @return 존재여부
     */
    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM tech_stack WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    // ==================== PRIVATE METHODS ====================

    /**
     * ResultSet의 각 행을 TechStack 객체로 변환
     */
    private final RowMapper<TechStack> techStackRowMapper = (rs, rowMapper) -> {
        TechStack techStack = new TechStack();
        techStack.setId(rs.getLong("id"));
        techStack.setProfileId(rs.getLong("profile_id"));
        techStack.setName(rs.getString("name"));
        techStack.setCategory(TechCategory.valueOf(rs.getString("category")));
        techStack.setProficiency(Proficiency.valueOf(rs.getString("proficiency")));
        techStack.setYearsOfExp(rs.getInt("years_of_exp"));
        techStack.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        techStack.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return techStack;
    };
}
