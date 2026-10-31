package com.enterprise.identityauditor.repository;

import com.enterprise.identityauditor.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    List<Permission> findByIsElevatedTrue();
    List<Permission> findByCategory(String category);
}
