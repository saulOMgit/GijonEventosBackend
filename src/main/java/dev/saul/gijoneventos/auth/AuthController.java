package dev.saul.gijoneventos.auth;

import dev.saul.gijoneventos.user.UserService;
import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserDTOResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${api-endpoint}")
public class AuthController {
    
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public ResponseEntity<UserDTOResponse> login(Authentication authentication) {
        // Obtener el username del usuario autenticado
        String username = authentication.getName();
        
        // Buscar el usuario en la base de datos
        UserEntity user = userService.findByUsername(username);
        
        // Crear DTO con los campos esperados por el frontend
        UserDTOResponse userDTO = new UserDTOResponse(
            user.getId(),
            user.getFullName(),
            user.getUsername(),
            user.getEmail(),
            user.getPhone()
        );

        return ResponseEntity.ok(userDTO);
    }
}

/* version Giaco
 package dev.saul.gijoneventos.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

/* 
 * https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/basic.html
 

@RestController
@RequestMapping(path = "${api-endpoint}")
public class AuthController {
    
    @GetMapping("/login")
    public ResponseEntity<AuthDTOResponse> login() {
        
        SecurityContext contextHolder = SecurityContextHolder.getContext();
        Authentication auth = contextHolder.getAuthentication();
        
        AuthDTOResponse authResponse = new AuthDTOResponse("Logged", auth.getName(), auth.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.accepted().body(authResponse);
    }
    

}
 */