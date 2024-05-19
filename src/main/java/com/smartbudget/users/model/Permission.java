package com.smartbudget.users.model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "permissions", schema = "users_auth")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    private String permission;
}
