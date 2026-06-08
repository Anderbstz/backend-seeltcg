package com.pikacards.user.controller;

import com.pikacards.auth.model.Role;
import com.pikacards.auth.model.User;
import com.pikacards.user.model.Profile;
import com.pikacards.user.repository.ProfileRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileRepository profileRepository;

    public ProfileController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            return ResponseEntity.ok(Map.of(
                "avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
            ));
        }

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Profile p = new Profile();
                    p.setUser(user);
                    return profileRepository.save(p);
                });

        return ResponseEntity.ok(Map.of(
            "firstName", profile.getFirstName() != null ? profile.getFirstName() : "",
            "lastName", profile.getLastName() != null ? profile.getLastName() : "",
            "avatar", user.getAvatarUrl() != null ? user.getAvatarUrl() : "",
            "imageSize", profile.getImageSize() != null ? profile.getImageSize() : "medium",
            "province", profile.getProvince() != null ? profile.getProvince() : "",
            "address", profile.getAddress() != null ? profile.getAddress() : ""
        ));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody ProfileUpdateRequest request) {
        if (user.getRole() == Role.ROLE_ADMIN) {
            if (request.getAvatar() != null) {
                user.setAvatarUrl(request.getAvatar());
            }
            return ResponseEntity.ok(Map.of("message", "Perfil actualizado"));
        }

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Profile p = new Profile();
                    p.setUser(user);
                    return p;
                });

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getImageSize() != null) profile.setImageSize(request.getImageSize());
        if (request.getProvince() != null) profile.setProvince(request.getProvince());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());

        profileRepository.save(profile);

        if (request.getAvatar() != null) {
            user.setAvatarUrl(request.getAvatar());
        }

        return ResponseEntity.ok(Map.of("message", "Perfil actualizado correctamente"));
    }

    public static class ProfileUpdateRequest {
        @Size(max = 150)
        private String firstName;
        @Size(max = 150)
        private String lastName;
        private String imageSize;
        @Size(max = 100)
        private String province;
        @Size(max = 500)
        private String address;
        @Size(max = 500)
        private String avatar;

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getImageSize() { return imageSize; }
        public void setImageSize(String imageSize) { this.imageSize = imageSize; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
}
