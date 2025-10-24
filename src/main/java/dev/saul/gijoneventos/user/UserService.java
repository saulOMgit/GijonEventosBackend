package dev.saul.gijoneventos.user;

import dev.saul.gijoneventos.role.RoleEntity;
import dev.saul.gijoneventos.role.RoleRepository; // Necesitarás este repositorio
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Nuevo
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity save(UserEntity user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public UserEntity findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
    }

    public UserEntity register(UserDTORequest registerData) {
        // Buscar el rol "ROLE_CLIENT" (o "ROLE_USER", según tu convención)
        RoleEntity userRole = roleRepository.findByName("ROLE_CLIENT")
            .orElseThrow(() -> new RuntimeException("Rol ROLE_CLIENT no encontrado. Asegúrate de que esté creado en la base de datos."));

        UserEntity user = UserEntity.builder()
            .fullName(registerData.getFullName())
            .username(registerData.getUsername())
            .email(registerData.getEmail())
            .phone(registerData.getPhone())
            .password(passwordEncoder.encode(registerData.getPassword()))
            .roles(Set.of(userRole)) // Asignar rol existente
            .build();
        
        return userRepository.save(user);
    }
}

/* Giaco version
 package dev.saul.gijoneventos.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

} */