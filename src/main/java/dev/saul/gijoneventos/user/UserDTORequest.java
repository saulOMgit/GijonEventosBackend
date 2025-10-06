package dev.saul.gijoneventos.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTORequest {

    private String fullName;
    private String username; // DNI/NIE
    private String email;
    private String phone;
    private String password;
    private String confirmPassword;

    // TODO: Añadir campos de eventos cuando se requieran más adelante
    // private List<EventRequest> events;

}
