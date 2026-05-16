package com.reminder.reminder_service.controller;

import com.reminder.reminder_service.dto.request.UpdateNextRunTimeRequest;
import com.reminder.reminder_service.entity.Reminder;
import com.reminder.reminder_service.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/reminders")
@RequiredArgsConstructor
public class InternalReminderController {

    private final ReminderService reminderService;

    @GetMapping("/due")
    public ResponseEntity<List<Reminder>> getDueReminders(@RequestParam OffsetDateTime now) {
        return ResponseEntity.ok(reminderService.findDueReminders(now));
    }

    @PutMapping("/{id}/next-run-time")
    public ResponseEntity<Void> updateNextRunTime(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNextRunTimeRequest request) {
        reminderService.updateNextRunTime(id, request.getNextRunTime());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/compute-next-occurrence")
    public ResponseEntity<OffsetDateTime> computeNextOccurrence(
            @RequestParam String rrule,
            @RequestParam OffsetDateTime currentTime) {
        OffsetDateTime nextOccurrence = reminderService.computeNextOccurrence(rrule, currentTime);
        return ResponseEntity.ok(nextOccurrence);
    }
}
