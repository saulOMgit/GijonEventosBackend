package dev.saul.gijoneventos.user;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTOResponse {

    private Long id;
    private String fullName;
    private String username; // DNI/NIE
    private String email;
    private String phone;
    private String role;
    // TODO: Añadir información de eventos y citas cuando se requieran más adelante
    // private List<EventResponse> events;
}