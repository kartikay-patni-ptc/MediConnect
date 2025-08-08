package com.example.demo.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // Set false if account expiration handling is needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // Set false if locking users is implemented
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // Set false if password expiry is implemented
    }

    @Override
    public boolean isEnabled() {
        return true;  // Set false if admin/user disabling is used
    }

    public User getUser() {
        return user;  // Helpful if you want access to full user entity later
    }
}
