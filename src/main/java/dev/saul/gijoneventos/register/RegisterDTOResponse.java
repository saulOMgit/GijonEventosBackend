package dev.saul.gijoneventos.register;

import lombok.Builder;

@Builder
public record RegisterDTOResponse(
        Long id,
        String fullName,
        String username,
        String email,
        String phone
) {}