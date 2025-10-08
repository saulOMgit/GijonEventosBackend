package dev.saul.gijoneventos.register;

import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserRepository;
import dev.saul.gijoneventos.role.RoleEntity;
import dev.saul.gijoneventos.role.RoleRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class RegisterServiceImpl implements RegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegisterValidator validator;

    public RegisterServiceImpl(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               RegisterValidator validator) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @Override
    public RegisterDTOResponse registerUser(RegisterDTORequest dto) {

        // Validar datos básicos (ej: email, teléfono, etc.)
        validator.validate(dto);

        // Validar contraseñas
        if (!dto.password().equals(dto.confirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Comprobar si el usuario ya existe
        Optional<UserEntity> existingUser = userRepository.findByUsername(dto.username());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException(dto.username());
        }

        // Rol por defecto
        RoleEntity clientRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Rol USER no encontrado"));

        // Encriptar password
        String hashedPassword = passwordEncoder.encode(dto.password());

        // Mapear DTO → UserEntity
        UserEntity newUser = RegisterMapper.dtoToEntity(dto, hashedPassword, clientRole);

        // Guardar en BD
        UserEntity savedUser = userRepository.save(newUser);

        // Devolver respuesta (incluyendo el id generado)
        return RegisterDTOResponse.builder()
                .id(savedUser.getId()) // 👈 ahora se incluye el id
                .fullName(savedUser.getFullName())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .build();
    }
}
