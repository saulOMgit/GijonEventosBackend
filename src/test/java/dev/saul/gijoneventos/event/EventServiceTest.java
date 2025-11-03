package dev.saul.gijoneventos.event;

import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Event Service Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private UserEntity testUser;
    private EventEntity testEvent;
    private EventDTORequest testEventDTO;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = UserEntity.builder()
                .id(1L)
                .fullName("Test User")
                .username("testuser")
                .email("test@example.com")
                .phone("123456789")
                .password("password")
                .build();

        // Setup test event
        testEvent = EventEntity.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .date(LocalDateTime.of(2025, 12, 31, 20, 0))
                .location("Test Location")
                .organizer(testUser)
                .attendees(new HashSet<>())
                .maxAttendees(100)
                .build();

        // Setup test DTO
        testEventDTO = EventDTORequest.builder()
                .title("Test Event")
                .description("Test Description")
                .date("2025-12-31T20:00:00")
                .location("Test Location")
                .maxAttendees(100)
                .build();
    }

    @Test
    @DisplayName("Should find all events when no filter is provided")
    void testFindAll_NoFilter() {
        // Given
        List<EventEntity> events = List.of(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        // When
        List<EventEntity> result = eventService.findAll(EventFilter.ALL, testUser);

        // Then
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getTitle(), is("Test Event"));
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find events user is attending")
    void testFindAll_AttendingFilter() {
        // Given
        List<EventEntity> events = List.of(testEvent);
        when(eventRepository.findByAttendeesContaining(testUser)).thenReturn(events);

        // When
        List<EventEntity> result = eventService.findAll(EventFilter.ATTENDING, testUser);

        // Then
        assertThat(result, hasSize(1));
        verify(eventRepository, times(1)).findByAttendeesContaining(testUser);
    }

    @Test
    @DisplayName("Should find events organized by user")
    void testFindAll_OrganizedFilter() {
        // Given
        List<EventEntity> events = List.of(testEvent);
        when(eventRepository.findByOrganizer(testUser)).thenReturn(events);

        // When
        List<EventEntity> result = eventService.findAll(EventFilter.ORGANIZED, testUser);

        // Then
        assertThat(result, hasSize(1));
        verify(eventRepository, times(1)).findByOrganizer(testUser);
    }

    @Test
    @DisplayName("Should find event by ID successfully")
    void testFindById_Success() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // When
        EventEntity result = eventService.findById(1L);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getTitle(), is("Test Event"));
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void testFindById_NotFound() {
        // Given
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> eventService.findById(999L)
        );
        
        assertThat(exception.getMessage(), containsString("Evento no encontrado con ID: 999"));
    }

    @Test
    @DisplayName("Should create event from DTO successfully")
    void testCreateFromDTO_Success() {
        // Given
        when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        EventEntity result = eventService.createFromDTO(testEventDTO, testUser);

        // Then
        assertThat(result, is(notNullValue()));
        assertThat(result.getOrganizer(), is(testUser));
        verify(eventRepository, times(1)).save(any(EventEntity.class));
    }

    @Test
    @DisplayName("Should add organizer as attendee when creating event")
    void testCreateFromDTO_OrganizerIsAttendee() {
        // Given
        EventEntity savedEvent = EventEntity.builder()
                .id(1L)
                .title("Test Event")
                .description("Test Description")
                .date(LocalDateTime.of(2025, 12, 31, 20, 0))
                .location("Test Location")
                .organizer(testUser)
                .attendees(new HashSet<>(List.of(testUser)))
                .maxAttendees(100)
                .build();
        
        when(eventRepository.save(any(EventEntity.class))).thenReturn(savedEvent);

        // When
        EventEntity result = eventService.createFromDTO(testEventDTO, testUser);

        // Then
        assertThat(result.getAttendees(), hasItem(testUser));
    }

    @Test
    @DisplayName("Should update event successfully")
    void testUpdateEvent_Success() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        EventEntity result = eventService.updateEvent(1L, testEventDTO);

        // Then
        assertThat(result.getTitle(), is("Test Event"));
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    @DisplayName("Should join event successfully")
    void testJoinEvent_Success() {
        // Given
        UserEntity newUser = UserEntity.builder()
                .id(2L)
                .fullName("New User")
                .username("newuser")
                .email("new@example.com")
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        eventService.joinEvent(1L, 2L);

        // Then
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    @DisplayName("Should throw exception when event is full")
    void testJoinEvent_EventFull() {
        // Given
        testEvent.setMaxAttendees(1);
        testEvent.getAttendees().add(testUser);

        UserEntity newUser = UserEntity.builder()
                .id(2L)
                .username("newuser")
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> eventService.joinEvent(1L, 2L)
        );
        
        assertThat(exception.getMessage(), containsString("El evento está completo"));
    }

    @Test
    @DisplayName("Should leave event successfully")
    void testLeaveEvent_Success() {
        // Given
        testEvent.getAttendees().add(testUser);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.save(any(EventEntity.class))).thenReturn(testEvent);

        // When
        eventService.leaveEvent(1L, 1L);

        // Then
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    @DisplayName("Should delete event successfully")
    void testDeleteEvent_Success() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        doNothing().when(eventRepository).delete(testEvent);

        // When
        eventService.deleteEvent(1L);

        // Then
        verify(eventRepository, times(1)).delete(testEvent);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent event")
    void testDeleteEvent_NotFound() {
        // Given
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> eventService.deleteEvent(999L));
    }
}