package com.reminder.reminder_service.repository;

import com.reminder.reminder_service.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Reminder> findByIdAndUserId(UUID id, UUID userId);
}
