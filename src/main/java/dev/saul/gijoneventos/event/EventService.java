package dev.saul.gijoneventos.event;

import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public List<EventEntity> findAll(EventFilter filter, UserEntity user) {
        if (filter == null || filter == EventFilter.ALL) {
            return eventRepository.findAll();
        }
        return switch (filter) {
            case ATTENDING -> eventRepository.findByAttendeesContaining(user);
            case ORGANIZED -> eventRepository.findByOrganizer(user);
            default -> eventRepository.findAll();
        };
    }

    public EventEntity findById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Evento no encontrado con ID: " + id));
    }

    public EventEntity createFromDTO(EventDTORequest dto, UserEntity organizer) {
        EventEntity event = new EventEntity();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setDate(LocalDateTime.parse(dto.getDate()));
        event.setLocation(dto.getLocation());
        event.setOrganizer(organizer);
        event.setMaxAttendees(dto.getMaxAttendees());
        
        // Añadir automáticamente al organizador como asistente
        event.getAttendees().add(organizer);
        
        return eventRepository.save(event);
    }

    public EventEntity updateEvent(Long id, EventDTORequest dto) {
        EventEntity event = findById(id);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setDate(LocalDateTime.parse(dto.getDate()));
        event.setLocation(dto.getLocation());
        event.setMaxAttendees(dto.getMaxAttendees());
        return eventRepository.save(event);
    }

    public void joinEvent(Long id, Long userId) {
        EventEntity event = findById(id);
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));
        if (event.getAttendees().size() >= event.getMaxAttendees()) {
            throw new IllegalStateException("El evento está completo");
        }
        event.getAttendees().add(user);
        eventRepository.save(event);
    }

    public void leaveEvent(Long id, Long userId) {
        EventEntity event = findById(id);
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con ID: " + userId));
        event.getAttendees().remove(user);
        eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        EventEntity event = findById(id);
        eventRepository.delete(event);
    }
}