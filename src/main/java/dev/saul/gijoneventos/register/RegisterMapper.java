package dev.saul.gijoneventos.register;

import java.util.Set;

import dev.saul.gijoneventos.role.RoleEntity;
import dev.saul.gijoneventos.user.UserEntity;

public class RegisterMapper {

    public static UserEntity dtoToEntity(RegisterDTORequest dto, String encodedPassword, RoleEntity defaultRole) {
        return UserEntity.builder()
                .fullName(dto.fullName())
                .username(dto.username())
                .email(dto.email())
                .phone(dto.phone())
                .password(encodedPassword) // solo guardamos la contraseña encriptada
                .roles(Set.of(defaultRole))
                .build();
    }
}
