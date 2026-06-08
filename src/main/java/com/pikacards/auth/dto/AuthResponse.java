package com.pikacards.auth.dto;

public class AuthResponse {
    private String message;
    private UserDto user;
    private String access;
    private String refresh;

    public AuthResponse(String message, UserDto user, String access, String refresh) {
        this.message = message; this.user = user; this.access = access; this.refresh = refresh;
    }

    public String getMessage() { return message; } public void setMessage(String m) { message = m; }
    public UserDto getUser() { return user; } public void setUser(UserDto u) { user = u; }
    public String getAccess() { return access; } public void setAccess(String a) { access = a; }
    public String getRefresh() { return refresh; } public void setRefresh(String r) { refresh = r; }

    public static class UserDto {
        private Long id; private String username; private String email;
        private String firstName; private String lastName; private String avatar;
        private String role;

        public UserDto(Long id, String username, String email, String firstName, String lastName, String avatar, String role) {
            this.id = id; this.username = username; this.email = email;
            this.firstName = firstName; this.lastName = lastName; this.avatar = avatar;
            this.role = role;
        }
        public Long getId() { return id; } public String getUsername() { return username; }
        public String getEmail() { return email; } public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; } public String getAvatar() { return avatar; }
        public String getRole() { return role; }
    }
}
