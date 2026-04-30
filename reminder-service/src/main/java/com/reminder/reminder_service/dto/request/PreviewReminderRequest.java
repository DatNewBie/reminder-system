package com.reminder.reminder_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreviewReminderRequest {

    @NotBlank(message = "RRULE is required")
    private String rrule;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    @NotNull
    @Min(value = 1, message = "Count must be at least 1")
    @Max(value = 50, message = "Count must be at most 50")
    private Integer count = 5;
}
