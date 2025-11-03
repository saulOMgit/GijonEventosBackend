package dev.saul.gijoneventos.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.saul.gijoneventos.user.UserEntity;
import dev.saul.gijoneventos.user.UserRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Event Controller Integration Tests")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;
    private UserEntity adminUser;
    private UserEntity otherUser;
    private EventEntity testEvent;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = userRepository.save(UserEntity.builder()
            .fullName("Test User")
            .username("testuser")
            .email("test@example.com")
            .phone("123456789")
            .password("$2a$12$8LegtLQWe717tIPvZeivjuqKnaAs5.bm0Q05.5GrAmcKzXw2NjoUO")
            .build());

        // Create admin user
        adminUser = userRepository.save(UserEntity.builder()
            .fullName("Admin User")
            .username("admin")
            .email("admin@example.com")
            .phone("111111111")
            .password("$2a$12$8LegtLQWe717tIPvZeivjuqKnaAs5.bm0Q05.5GrAmcKzXw2NjoUO")
            .build());

        // Create other user
        otherUser = userRepository.save(UserEntity.builder()
            .fullName("Other User")
            .username("otheruser")
            .email("other@example.com")
            .phone("222222222")
            .password("$2a$12$8LegtLQWe717tIPvZeivjuqKnaAs5.bm0Q05.5GrAmcKzXw2NjoUO")
            .build());

        // Create test event
        testEvent = eventRepository.save(EventEntity.builder()
            .title("Test Event")
            .description("Test Description")
            .date(LocalDateTime.of(2025, 12, 31, 20, 0))
            .location("Test Location")
            .organizer(testUser)
            .maxAttendees(100)
            .build());
    }

    @Test
    @DisplayName("Should get all events when authenticated")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetEvents_Success() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].title", is("Test Event")))
            .andExpect(jsonPath("$[0].location", is("Test Location")));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void testGetEvents_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should create event successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testCreateEvent_Success() throws Exception {
        EventDTORequest newEvent = EventDTORequest.builder()
            .title("New Event")
            .description("New Description")
            .date("2025-12-31T20:00:00")
            .location("New Location")
            .maxAttendees(50)
            .build();

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEvent)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("New Event")))
            .andExpect(jsonPath("$.description", is("New Description")))
            .andExpect(jsonPath("$.maxAttendees", is(50)));
    }

    @Test
    @DisplayName("Should return 400 when creating event with invalid data")
    @WithMockUser(username = "testuser", roles = "USER")
    void testCreateEvent_InvalidData() throws Exception {
        EventDTORequest invalidEvent = EventDTORequest.builder()
            .title("")  // Empty title
            .description("Description")
            .date("2025-12-31T20:00:00")
            .location("Location")
            .maxAttendees(-10)  // Negative attendees
            .build();

        mockMvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEvent)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should update event when user is organizer")
    @WithMockUser(username = "testuser", roles = "USER")
    void testUpdateEvent_Organizer() throws Exception {
        EventDTORequest updatedEvent = EventDTORequest.builder()
            .title("Updated Event")
            .description("Updated Description")
            .date("2025-12-31T20:00:00")
            .location("Updated Location")
            .maxAttendees(150)
            .build();

        mockMvc.perform(put("/api/v1/events/" + testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Updated Event")))
            .andExpect(jsonPath("$.maxAttendees", is(150)));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when non-organizer tries to update event")
    @WithMockUser(username = "otheruser", roles = "USER")
    void testUpdateEvent_NotOrganizer() throws Exception {
        EventDTORequest updatedEvent = EventDTORequest.builder()
            .title("Updated Event")
            .description("Updated Description")
            .date("2025-12-31T20:00:00")
            .location("Updated Location")
            .maxAttendees(150)
            .build();

        ServletException thrown = assertThrows(ServletException.class, () ->
            mockMvc.perform(put("/api/v1/events/" + testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEvent)))
        );

        assertInstanceOf(IllegalStateException.class, thrown.getCause());
        assertEquals("Solo el organizador o un administrador puede editar el evento", thrown.getCause().getMessage());
    }

    @Test
    @DisplayName("Should join event successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testJoinEvent_Success() throws Exception {
        mockMvc.perform(post("/api/v1/events/" + testEvent.getId() + "/join"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should leave event successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testLeaveEvent_Success() throws Exception {
        // First join
        mockMvc.perform(post("/api/v1/events/" + testEvent.getId() + "/join"))
            .andExpect(status().isOk());

        // Then leave
        mockMvc.perform(post("/api/v1/events/" + testEvent.getId() + "/leave"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should delete event when user is organizer")
    @WithMockUser(username = "testuser", roles = "USER")
    void testDeleteEvent_Organizer() throws Exception {
        mockMvc.perform(delete("/api/v1/events/" + testEvent.getId()))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin should be able to delete any event")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testDeleteEvent_Admin() throws Exception {
        mockMvc.perform(delete("/api/v1/events/" + testEvent.getId()))
            .andExpect(status().isOk());
    }
}
