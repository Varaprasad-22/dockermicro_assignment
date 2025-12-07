package com.auth.service;

import org.springframework.stereotype.Service;

import com.auth.model.User;
import com.auth.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.Set;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void createUser(RegisterRequest req) {
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPassword(encoder.encode(req.getPassword()));
        u.setRoles(Set.of("ROLE_USER"));
        repo.save(u);
    }

    public boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }
}
