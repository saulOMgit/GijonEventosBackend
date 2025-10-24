package dev.saul.gijoneventos.event;

import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserRepository;
import dev.saul.gijoneventos.user.UserDTOResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "${api-endpoint}/events")
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    public EventController(EventService eventService, UserRepository userRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<EventDTOResponse>> getEvents(@RequestParam(required = false) EventFilter filter, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        List<EventEntity> events = eventService.findAll(filter, user);
        List<EventDTOResponse> response = events.stream().map(this::toDTOResponse).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<EventDTOResponse> createEvent(@Valid @RequestBody EventDTORequest dto, Authentication authentication) {
        UserEntity organizer = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        EventEntity event = eventService.createFromDTO(dto, organizer);
        return ResponseEntity.ok(toDTOResponse(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDTOResponse> updateEvent(@PathVariable Long id, @Valid @RequestBody EventDTORequest dto, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        EventEntity event = eventService.findById(id); // findById lanza excepción si no encuentra
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!event.getOrganizer().getId().equals(user.getId()) && !isAdmin) {
            throw new IllegalStateException("Solo el organizador o un administrador puede editar el evento");
        }
        EventEntity updated = eventService.updateEvent(id, dto);
        return ResponseEntity.ok(toDTOResponse(updated));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> joinEvent(@PathVariable Long id, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        eventService.joinEvent(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<Void> leaveEvent(@PathVariable Long id, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        eventService.leaveEvent(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Authentication authentication) {
        UserEntity user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        EventEntity event = eventService.findById(id); // findById lanza excepción si no encuentra
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!event.getOrganizer().getId().equals(user.getId()) && !isAdmin) {
            throw new IllegalStateException("Solo el organizador o un administrador puede eliminar el evento");
        }
        eventService.deleteEvent(id);
        return ResponseEntity.ok().build();
    }

    private EventDTOResponse toDTOResponse(EventEntity event) {
        return EventDTOResponse.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .date(event.getDate().toString())
            .location(event.getLocation())
            .organizer(new UserDTOResponse(
                event.getOrganizer().getId(),
                event.getOrganizer().getFullName(),
                event.getOrganizer().getUsername(),
                event.getOrganizer().getEmail(),
                event.getOrganizer().getPhone(),
                "" // Valor por defecto para role
            ))
            .attendees(event.getAttendees().stream().map(UserEntity::getId).collect(Collectors.toSet()))
            .maxAttendees(event.getMaxAttendees())
            .build();
    }
}