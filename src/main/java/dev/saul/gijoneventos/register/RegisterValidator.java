package dev.saul.gijoneventos.register;

import org.springframework.stereotype.Component; 

@Component
public class RegisterValidator {

    public void validate(RegisterDTORequest dto) {
        if (dto.username() == null || dto.username().isBlank())
            throw new IllegalArgumentException("El DNI es obligatorio");
        if (dto.password() == null || dto.password().isBlank())
            throw new IllegalArgumentException("La contraseña es obligatoria");
        if (!dto.password().equals(dto.confirmPassword()))
            throw new IllegalArgumentException("Las contraseñas no coinciden");
    }
}
