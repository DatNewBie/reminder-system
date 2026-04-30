package com.reminder.reminder_service.controller;

import com.reminder.reminder_service.dto.request.CreateReminderRequest;
import com.reminder.reminder_service.dto.request.PreviewReminderRequest;
import com.reminder.reminder_service.dto.request.UpdateReminderRequest;
import com.reminder.reminder_service.dto.response.ReminderResponse;
import com.reminder.reminder_service.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ReminderResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateReminderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reminderService.createReminder(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponse>> getAll(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(reminderService.getReminders(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReminderResponse> getOne(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(reminderService.getReminder(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderResponse> update(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReminderRequest request) {
        return ResponseEntity.ok(reminderService.updateReminder(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        reminderService.deleteReminder(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preview")
    public ResponseEntity<List<OffsetDateTime>> preview(
            @Valid @RequestBody PreviewReminderRequest request) {
        return ResponseEntity.ok(reminderService.previewReminder(request));
    }
}
