package com.example.photoGroupe.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    SUPER_ADMIN,
    ADMIN,
    PHOTOGRAPHER,
    USER;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
