package com.reminder.reminder_service.repository;

import com.reminder.reminder_service.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Reminder> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT r FROM Reminder r WHERE r.isActive = true AND r.nextRunTime <= :now ORDER BY r.nextRunTime ASC")
    List<Reminder> findDueReminders(@Param("now") OffsetDateTime now);
}
