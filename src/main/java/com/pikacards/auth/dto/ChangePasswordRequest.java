package com.pikacards.auth.dto;
import jakarta.validation.constraints.NotBlank;
public class ChangePasswordRequest {
    @NotBlank private String currentPassword;
    @NotBlank private String newPassword;
    public String getCurrentPassword() { return currentPassword; } public void setCurrentPassword(String p) { currentPassword = p; }
    public String getNewPassword() { return newPassword; } public void setNewPassword(String p) { newPassword = p; }
}
