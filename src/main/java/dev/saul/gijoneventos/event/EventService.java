package dev.saul.gijoneventos.event;

import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public EventEntity save(EventEntity event) {
        return eventRepository.save(event);
    }

    @Transactional
    public EventEntity createFromDTO(EventDTORequest dto, UserEntity organizer) {
        LocalDateTime parsedDate = LocalDateTime.parse(dto.getDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return save(EventEntity.builder()
            .title(dto.getTitle())
            .description(dto.getDescription())
            .date(parsedDate)
            .location(dto.getLocation())
            .organizer(organizer)
            .maxAttendees(dto.getMaxAttendees())
            .build());
    }

    @Transactional
    public EventEntity updateEvent(Long eventId, EventDTORequest dto) {
        EventEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        LocalDateTime parsedDate = LocalDateTime.parse(dto.getDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setDate(parsedDate);
        event.setLocation(dto.getLocation());
        event.setMaxAttendees(dto.getMaxAttendees());
        return save(event);
    }

    @Transactional
    public void joinEvent(Long eventId, Long userId) {
        EventEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (event.getAttendees().size() >= event.getMaxAttendees()) {
            throw new IllegalStateException("El evento está completo");
        }
        event.getAttendees().add(user);
        eventRepository.save(event);
    }

    @Transactional
    public void leaveEvent(Long eventId, Long userId) {
        EventEntity event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        event.getAttendees().remove(user);
        eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    public List<EventEntity> findAll() {
        return eventRepository.findAll();
    }

    public Optional<EventEntity> findById(Long id) {
        return eventRepository.findById(id);
    }
}