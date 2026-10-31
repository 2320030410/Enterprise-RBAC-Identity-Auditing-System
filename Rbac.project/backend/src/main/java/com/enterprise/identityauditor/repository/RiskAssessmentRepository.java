package com.enterprise.identityauditor.repository;

import com.enterprise.identityauditor.entity.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    Optional<RiskAssessment> findByUserId(Long userId);

    List<RiskAssessment> findAllByOrderByRiskScoreDesc();

    long countByRiskClassification(String riskClassification);

    @Query("SELECT r FROM RiskAssessment r WHERE " +
           "(:classification IS NULL OR :classification = '' OR r.riskClassification = :classification) " +
           "AND (:department IS NULL OR :department = '' OR r.department = :department) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(r.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(r.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY r.riskScore DESC")
    List<RiskAssessment> filterAssessments(@Param("classification") String classification,
                                          @Param("department") String department,
                                          @Param("keyword") String keyword);
}
