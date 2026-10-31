package com.enterprise.identityauditor.repository;

import com.enterprise.identityauditor.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    List<AuditLog> findTop15ByOrderByTimestampDesc();

    // Critical for privilege creep: find the most recent successful audit log for a user executing a specific action
    Optional<AuditLog> findTop1ByUserIdAndActionAndStatusOrderByTimestampDesc(Long userId, String action, String status);

    // Find the most recent privileged activity timestamp for a user
    @Query("SELECT MAX(a.timestamp) FROM AuditLog a WHERE a.userId = :userId AND a.status = 'SUCCESS' " +
           "AND a.action IN :privilegedActions")
    Optional<LocalDateTime> findLatestPrivilegedActivityTime(@Param("userId") Long userId,
                                                            @Param("privilegedActions") List<String> privilegedActions);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:username IS NULL OR :username = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:action IS NULL OR :action = '' OR a.action = :action) " +
           "AND (:status IS NULL OR :status = '' OR a.status = :status) " +
           "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
           "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    List<AuditLog> searchAuditLogs(@Param("username") String username,
                                  @Param("action") String action,
                                  @Param("status") String status,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:username IS NULL OR :username = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:action IS NULL OR :action = '' OR a.action = :action) " +
           "AND (:status IS NULL OR :status = '' OR a.status = :status) " +
           "AND (:startDate IS NULL OR a.timestamp >= :startDate) " +
           "AND (:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLogsPaged(@Param("username") String username,
                                       @Param("action") String action,
                                       @Param("status") String status,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       Pageable pageable);

    @Query("SELECT DISTINCT a.action FROM AuditLog a")
    List<String> findDistinctActions();
}
