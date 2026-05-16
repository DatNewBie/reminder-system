package com.reminder.reminder_service.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateNextRunTimeRequest {

    @NotNull(message = "Next run time is required")
    @FutureOrPresent(message = "Next run time must be in the present or future")
    private OffsetDateTime nextRunTime;
}
