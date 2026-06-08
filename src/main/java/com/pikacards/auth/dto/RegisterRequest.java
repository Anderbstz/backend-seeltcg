package com.pikacards.auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public class RegisterRequest {
    @NotBlank @Size(min = 3, max = 150) private String username;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 6) private String password;
    public String getUsername() { return username; } public void setUsername(String u) { username = u; }
    public String getEmail() { return email; } public void setEmail(String e) { email = e; }
    public String getPassword() { return password; } public void setPassword(String p) { password = p; }
}
