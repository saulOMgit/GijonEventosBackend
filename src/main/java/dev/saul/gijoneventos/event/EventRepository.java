package dev.saul.gijoneventos.event;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.saul.gijoneventos.user.UserEntity;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByAttendeesContaining(UserEntity user);
    List<EventEntity> findByOrganizer(UserEntity organizer);
}