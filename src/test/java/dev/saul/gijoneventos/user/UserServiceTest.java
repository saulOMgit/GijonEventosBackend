package dev.saul.gijoneventos.user;

import dev.saul.gijoneventos.role.RoleEntity;
import dev.saul.gijoneventos.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;
    private RoleEntity userRole;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
            .id(1L)
            .fullName("Test User")
            .username("testuser")
            .email("test@example.com")
            .phone("123456789")
            .password("plainPassword")
            .build();

        userRole = RoleEntity.builder()
            .id(2L)
            .name("ROLE_CLIENT")
            .build();
    }

    @Test
    @DisplayName("Should save user with encoded password")
    void testSave_EncodesPassword() {
        // Given
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$12$encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // When
        UserEntity savedUser = userService.save(testUser);

        // Then
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(testUser);
        assertThat(savedUser, is(notNullValue()));
    }

    @Test
    @DisplayName("Should find user by username")
    void testFindByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserEntity foundUser = userService.findByUsername("testuser");

        // Then
        assertThat(foundUser, is(notNullValue()));
        assertThat(foundUser.getUsername(), is("testuser"));
        assertThat(foundUser.getEmail(), is("test@example.com"));
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testFindByUsername_NotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.findByUsername("nonexistent")
        );

        assertThat(exception.getMessage(), containsString("Usuario no encontrado: nonexistent"));
    }

    @Test
    @DisplayName("Should register user with CLIENT role")
    void testRegister_AssignsClientRole() {
        // Given
        UserDTORequest registerData = UserDTORequest.builder()
            .fullName("New User")
            .username("newuser")
            .email("new@example.com")
            .phone("987654321")
            .password("password123")
            .build();

        UserEntity savedUser = UserEntity.builder()
            .id(2L)
            .fullName("New User")
            .username("newuser")
            .email("new@example.com")
            .phone("987654321")
            .password("$2a$12$encoded")
            .roles(Set.of(userRole))
            .build();

        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$encoded");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        UserEntity registeredUser = userService.register(registerData);

        // Then
        assertThat(registeredUser, is(notNullValue()));
        assertThat(registeredUser.getRoles(), hasSize(1));
        assertThat(registeredUser.getRoles(), hasItem(hasProperty("name", is("ROLE_CLIENT"))));
        verify(roleRepository, times(1)).findByName("ROLE_CLIENT");
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("Should throw exception when ROLE_CLIENT not found during registration")
    void testRegister_RoleNotFound() {
        // Given
        UserDTORequest registerData = UserDTORequest.builder()
            .fullName("New User")
            .username("newuser")
            .email("new@example.com")
            .phone("987654321")
            .password("password123")
            .build();

        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.register(registerData)
        );

        assertThat(exception.getMessage(), containsString("Rol ROLE_CLIENT no encontrado"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Should encode password during registration")
    void testRegister_EncodesPassword() {
        // Given
        UserDTORequest registerData = UserDTORequest.builder()
            .fullName("New User")
            .username("newuser")
            .email("new@example.com")
            .phone("987654321")
            .password("plainPassword")
            .build();

        UserEntity savedUser = UserEntity.builder()
            .id(2L)
            .fullName("New User")
            .username("newuser")
            .email("new@example.com")
            .phone("987654321")
            .password("$2a$12$encodedPassword")
            .roles(Set.of(userRole))
            .build();

        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$12$encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        UserEntity registeredUser = userService.register(registerData);

        // Then
        assertThat(registeredUser.getPassword(), startsWith("$2a$12$"));
        verify(passwordEncoder, times(1)).encode("plainPassword");
    }
}