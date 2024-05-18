package com.smartbudget.users.repository;

import com.smartbudget.users.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Permission findByPermission(String permission);
}