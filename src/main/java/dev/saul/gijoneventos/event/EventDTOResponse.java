package dev.saul.gijoneventos.event;

import dev.saul.gijoneventos.user.UserDTOResponse;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTOResponse {

    private Long id;
    private String title;
    private String description;
    private String date;  // Convertir LocalDateTime a string en el servicio (e.g., ISO format)
    private String location;
    private UserDTOResponse organizer;
    private Set<Long> attendees;  // IDs de usuarios para matching con frontend (string[])
    private int maxAttendees;
}