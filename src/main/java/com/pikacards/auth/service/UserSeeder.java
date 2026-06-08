package com.pikacards.auth.service;

import com.pikacards.auth.model.Role;
import com.pikacards.auth.model.User;
import com.pikacards.auth.repository.UserRepository;
import com.pikacards.user.model.Profile;
import com.pikacards.user.repository.ProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(UserRepository userRepository, ProfileRepository profileRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUser("admin", "123123123", Role.ROLE_ADMIN);
        seedUser("user", "123123123", Role.ROLE_USER);
    }

    private void seedUser(String username, String rawPassword, Role role) {
        if (userRepository.findByUsername(username).isPresent()) return;

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);

        if (role == Role.ROLE_USER) {
            Profile profile = new Profile();
            profile.setUser(user);
            profile.setFirstName(username);
            profileRepository.save(profile);
        }
    }
}
