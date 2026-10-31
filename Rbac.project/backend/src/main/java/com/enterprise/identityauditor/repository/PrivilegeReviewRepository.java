package com.enterprise.identityauditor.repository;

import com.enterprise.identityauditor.entity.PrivilegeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrivilegeReviewRepository extends JpaRepository<PrivilegeReview, Long> {

    List<PrivilegeReview> findByStatusOrderByCreatedAtDesc(String status);

    List<PrivilegeReview> findAllByOrderByCreatedAtDesc();

    List<PrivilegeReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndPermissionNameAndStatus(Long userId, String permissionName, String status);

    Optional<PrivilegeReview> findTop1ByUserIdAndPermissionNameAndStatus(Long userId, String permissionName, String status);

    long countByStatus(String status);
}
