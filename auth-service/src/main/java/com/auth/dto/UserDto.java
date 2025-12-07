package com.auth.dto;


import java.util.Set;

public class UserDto {
    private String id;
    private String username;
    private Set<String> roles;

    public UserDto() {}

    public UserDto(String id, String username, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}
