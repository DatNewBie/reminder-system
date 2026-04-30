package com.reminder.reminder_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReminderRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String message;

    private String rrule;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    private Boolean isActive;
}
