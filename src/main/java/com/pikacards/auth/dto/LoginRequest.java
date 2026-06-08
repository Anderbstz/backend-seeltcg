package com.pikacards.auth.dto;
import jakarta.validation.constraints.NotBlank;
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
    public String getUsername() { return username; } public void setUsername(String u) { username = u; }
    public String getPassword() { return password; } public void setPassword(String p) { password = p; }
}
