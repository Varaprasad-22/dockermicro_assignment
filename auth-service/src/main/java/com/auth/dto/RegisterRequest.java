package com.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Username (email) is required")
    @Email(message = "Username must be a valid email")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Optional: allow client to request admin (use carefully)
    // It's safer to ignore this and only allow ROLE_USER by default.
    private String requestedRole;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String requestedRole) {
        this.username = username;
        this.password = password;
        this.requestedRole = requestedRole;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRequestedRole() { return requestedRole; }
    public void setRequestedRole(String requestedRole) { this.requestedRole = requestedRole; }
}
