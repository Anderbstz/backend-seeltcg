package com.pikacards.auth.service;

import com.pikacards.auth.dto.*;
import com.pikacards.auth.model.User;
import com.pikacards.auth.repository.UserRepository;
import com.pikacards.auth.security.JwtTokenProvider;
import com.pikacards.cart.repository.CartItemRepository;
import com.pikacards.email.service.EmailService;
import com.pikacards.order.repository.OrderRepository;
import com.pikacards.user.model.Profile;
import com.pikacards.user.repository.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final ProfileRepository profileRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final String googleClientId;
    private final EmailService emailService;
    private final RestTemplate restTemplate;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
                       ProfileRepository profileRepository, OrderRepository orderRepository,
                       CartItemRepository cartItemRepository,
                       @Value("${pikacards.google.client-id}") String googleClientId,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.profileRepository = profileRepository;
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.googleClientId = googleClientId;
        this.emailService = emailService;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new IllegalArgumentException("El usuario ya existe");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("El email ya está registrado");
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        Profile profile = new Profile();
        profile.setUser(user);
        profileRepository.save(profile);

        if (request.getEmail() != null) {
            emailService.sendWelcomeEmail(request.getEmail(), request.getUsername());
        }
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return buildAuthResponse(user, "Login correcto");
    }

    public AuthResponse googleLogin(GoogleLoginRequest request) {
        String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getToken();
        ResponseEntity<Map> googleResponse;
        try {
            googleResponse = restTemplate.getForEntity(verifyUrl, Map.class);
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error al verificar el token de Google");
        }
        if (googleResponse.getStatusCode() != HttpStatus.OK || googleResponse.getBody() == null)
            throw new IllegalArgumentException("Token de Google inválido o expirado");
        Map<String, Object> data = googleResponse.getBody();

        String tokenAud = (String) data.get("aud");
        if (tokenAud == null || !tokenAud.equals(googleClientId)) {
            log.warn("Google token aud mismatch: expected {}, got {}", googleClientId, tokenAud);
            throw new IllegalArgumentException("Token de Google no corresponde a esta aplicación");
        }

        String email = (String) data.get("email");
        String name = (String) data.getOrDefault("name", email != null ? email.split("@")[0] : "user");
        String givenName = (String) data.getOrDefault("given_name", "");
        String familyName = (String) data.getOrDefault("family_name", "");
        String picture = (String) data.getOrDefault("picture", "");
        if (email == null) throw new IllegalArgumentException("Token no contiene email");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode("google-oauth-" + System.currentTimeMillis()));
            newUser.setAvatarUrl(picture);
            User saved = userRepository.save(newUser);

            Profile profile = new Profile();
            profile.setUser(saved);
            profile.setFirstName(givenName.isEmpty() ? name : givenName);
            profile.setLastName(familyName);
            profileRepository.save(profile);

            return saved;
        });
        boolean updated = false;
        if (!picture.isEmpty() && !picture.equals(user.getAvatarUrl())) { user.setAvatarUrl(picture); updated = true; }
        if (updated) userRepository.save(user);

        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        String displayName = profile != null ? profile.getFirstName() : null;

        AuthResponse.UserDto userDto = new AuthResponse.UserDto(user.getId(), user.getUsername(), user.getEmail(),
                displayName, profile != null ? profile.getLastName() : null, user.getAvatarUrl(), user.getRole().name());
        String accessToken = tokenProvider.generateToken(user.getId(), user.getUsername());
        return new AuthResponse("Login con Google OK", userDto, accessToken, accessToken);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword()))
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId, String confirm, String password) {
        if (!"DELETE".equals(confirm)) throw new IllegalArgumentException("Confirma escribiendo DELETE");
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!passwordEncoder.matches(password, user.getPassword())) throw new IllegalArgumentException("Contraseña inválida");

        profileRepository.findByUserId(userId).ifPresent(profileRepository::delete);
        orderRepository.deleteByUser(user);
        cartItemRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    public AuthResponse.UserDto getProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        String displayName = profile != null ? profile.getFirstName() : null;
        return new AuthResponse.UserDto(user.getId(), user.getUsername(), user.getEmail(),
                displayName, profile != null ? profile.getLastName() : null, user.getAvatarUrl(), user.getRole().name());
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        String displayName = profile != null ? profile.getFirstName() : null;
        AuthResponse.UserDto userDto = new AuthResponse.UserDto(user.getId(), user.getUsername(), user.getEmail(),
                displayName, profile != null ? profile.getLastName() : null, user.getAvatarUrl(), user.getRole().name());
        String accessToken = tokenProvider.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(message, userDto, accessToken, accessToken);
    }
}
