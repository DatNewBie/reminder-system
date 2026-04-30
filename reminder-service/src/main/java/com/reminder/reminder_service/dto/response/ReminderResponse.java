package com.reminder.reminder_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String message;
    private String rrule;
    private OffsetDateTime startTime;
    private OffsetDateTime nextRunTime;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
