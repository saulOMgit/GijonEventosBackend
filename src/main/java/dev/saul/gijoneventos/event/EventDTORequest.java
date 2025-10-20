package dev.saul.gijoneventos.event;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDTORequest {

    @NotBlank(message = "El título es obligatorio")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotBlank(message = "La fecha es obligatoria")
    private String date;

    @NotBlank(message = "La ubicación es obligatoria")
    private String location;

    @Positive(message = "El número de asistentes debe ser mayor a 0")
    private int maxAttendees;
}