package com.reminder.reminder_service.service;

import com.reminder.reminder_service.dto.request.CreateReminderRequest;
import com.reminder.reminder_service.dto.request.PreviewReminderRequest;
import com.reminder.reminder_service.dto.request.UpdateReminderRequest;
import com.reminder.reminder_service.dto.response.ReminderResponse;
import com.reminder.reminder_service.entity.Reminder;
import com.reminder.reminder_service.exception.ReminderNotFoundException;
import com.reminder.reminder_service.exception.RruleValidationException;
import com.reminder.reminder_service.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.parameter.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;

    @Transactional
    public ReminderResponse createReminder(UUID userId, CreateReminderRequest req) {
        validateRrule(req.getRrule());

        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setTitle(req.getTitle());
        reminder.setMessage(req.getMessage());
        reminder.setRrule(req.getRrule());
        reminder.setStartTime(req.getStartTime());
        reminder.setNextRunTime(req.getStartTime());
        reminder.setIsActive(true);

        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional(readOnly = true)
    public List<ReminderResponse> getReminders(UUID userId) {
        return reminderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReminderResponse getReminder(UUID userId, UUID id) {
        return toResponse(findOwnedReminder(userId, id));
    }

    @Transactional
    public ReminderResponse updateReminder(UUID userId, UUID id, UpdateReminderRequest req) {
        validateRrule(req.getRrule());

        Reminder reminder = findOwnedReminder(userId, id);
        reminder.setTitle(req.getTitle());
        reminder.setMessage(req.getMessage());
        reminder.setRrule(req.getRrule());
        reminder.setStartTime(req.getStartTime());
        reminder.setNextRunTime(req.getStartTime());
        if (req.getIsActive() != null) {
            reminder.setIsActive(req.getIsActive());
        }

        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional
    public void deleteReminder(UUID userId, UUID id) {
        Reminder reminder = findOwnedReminder(userId, id);
        reminderRepository.delete(reminder);
    }

    public List<OffsetDateTime> previewReminder(PreviewReminderRequest req) {
        validateRrule(req.getRrule());
        return computeOccurrences(req.getRrule(), req.getStartTime(), req.getCount());
    }

    private Reminder findOwnedReminder(UUID userId, UUID id) {
        return reminderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ReminderNotFoundException(id));
    }

    private void validateRrule(String rrule) {
        if (rrule == null || rrule.isBlank()) return;
        try {
            new Recur(rrule);
        } catch (Exception e) {
            throw new RruleValidationException(rrule, e.getMessage());
        }
    }

    private List<OffsetDateTime> computeOccurrences(String rrule, OffsetDateTime startTime, int count) {
        try {
            Recur recur = new Recur(rrule);
            DateTime seed = new DateTime(startTime.toInstant().toEpochMilli());
            DateTime periodEnd = new DateTime(startTime.plusYears(5).toInstant().toEpochMilli());
            DateList dates = recur.getDates(seed, seed, periodEnd, Value.DATE_TIME);
            return dates.stream()
                    .limit(count)
                    .map(d -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), ZoneOffset.UTC))
                    .toList();
        } catch (RruleValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new RruleValidationException(rrule, e.getMessage());
        }
    }

    private ReminderResponse toResponse(Reminder r) {
        return ReminderResponse.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .title(r.getTitle())
                .message(r.getMessage())
                .rrule(r.getRrule())
                .startTime(r.getStartTime())
                .nextRunTime(r.getNextRunTime())
                .isActive(r.getIsActive())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
