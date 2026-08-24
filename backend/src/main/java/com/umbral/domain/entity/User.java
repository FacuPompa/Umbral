package com.umbral.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String handle;

    protected User(){}

    public User(String handle) {
        this.handle = handle;
    }

    public Long getId() {
        return id;
    }

    public String getHandle() {
        return handle;
    }
}
