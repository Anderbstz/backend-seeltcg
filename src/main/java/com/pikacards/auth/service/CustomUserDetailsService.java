package com.pikacards.auth.service;

import com.pikacards.auth.model.User;
import com.pikacards.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    public UserDetails loadUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con id: " + id));
    }
}
