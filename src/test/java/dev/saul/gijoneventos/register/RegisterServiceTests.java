package dev.saul.gijoneventos.register;

import dev.saul.gijoneventos.role.RoleEntity;
import dev.saul.gijoneventos.role.RoleRepository;
import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Register Service Tests")
class RegisterServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RegisterValidator validator;

    @InjectMocks
    private RegisterServiceImpl registerService;

    private RegisterDTORequest validRequest;
    private RoleEntity userRole;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterDTORequest(
            "John Doe",
            "johndoe",
            "john@example.com",
            "123456789",
            "password123",
            "password123"
        );

        userRole = RoleEntity.builder()
            .id(2L)
            .name("ROLE_USER")
            .build();
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() {
        // Given
        UserEntity savedUser = UserEntity.builder()
            .id(1L)
            .fullName("John Doe")
            .username("johndoe")
            .email("john@example.com")
            .phone("123456789")
            .password("encodedPassword")
            .build();

        doNothing().when(validator).validate(any(RegisterDTORequest.class));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        RegisterDTOResponse response = registerService.registerUser(validRequest);

        // Then
        assertThat(response, is(notNullValue()));
        assertThat(response.id(), is(1L));
        assertThat(response.fullName(), is("John Doe"));
        assertThat(response.username(), is("johndoe"));
        assertThat(response.email(), is("john@example.com"));
        
        verify(validator, times(1)).validate(validRequest);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when passwords do not match")
    void testRegisterUser_PasswordMismatch() {
        // Given
        RegisterDTORequest invalidRequest = new RegisterDTORequest(
            "John Doe",
            "johndoe",
            "john@example.com",
            "123456789",
            "password123",
            "differentPassword"
        );

        doNothing().when(validator).validate(any(RegisterDTORequest.class));

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registerService.registerUser(invalidRequest)
        );

        assertThat(exception.getMessage(), containsString("Las contraseñas no coinciden"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterUser_UsernameExists() {
        // Given
        UserEntity existingUser = UserEntity.builder()
            .id(1L)
            .username("johndoe")
            .build();

        doNothing().when(validator).validate(any(RegisterDTORequest.class));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(existingUser));

        // When & Then
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> registerService.registerUser(validRequest)
        );

        assertThat(exception.getMessage(), containsString("johndoe"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when ROLE_USER not found")
    void testRegisterUser_RoleNotFound() {
        // Given
        doNothing().when(validator).validate(any(RegisterDTORequest.class));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> registerService.registerUser(validRequest)
        );

        assertThat(exception.getMessage(), containsString("Rol USER no encontrado"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should encode password before saving")
    void testRegisterUser_PasswordEncoding() {
        // Given
        UserEntity savedUser = UserEntity.builder()
            .id(1L)
            .fullName("John Doe")
            .username("johndoe")
            .email("john@example.com")
            .phone("123456789")
            .password("$2a$12$encodedHash")
            .build();

        doNothing().when(validator).validate(any(RegisterDTORequest.class));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$12$encodedHash");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        registerService.registerUser(validRequest);

        // Then
        verify(passwordEncoder, times(1)).encode("password123");
    }
}