package com.example.photoGroupe.security;

import com.example.photoGroupe.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public Long getId()   { return user.getId(); }
    public User getUser() { return user; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return user.getAuthorities(); }
    @Override public String  getPassword()                { return user.getPassword(); }
    @Override public String  getUsername()                { return user.getUsername(); }  // returns email
    @Override public boolean isAccountNonExpired()        { return user.isAccountNonExpired(); }
    @Override public boolean isAccountNonLocked()         { return user.isAccountNonLocked(); }
    @Override public boolean isCredentialsNonExpired()    { return user.isCredentialsNonExpired(); }
    @Override public boolean isEnabled()                  { return user.isEnabled(); }
}