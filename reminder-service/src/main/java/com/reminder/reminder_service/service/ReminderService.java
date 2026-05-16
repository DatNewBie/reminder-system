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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ReminderService {

    private final ReminderRepository reminderRepository;

    @Transactional
    public ReminderResponse createReminder(UUID userId, CreateReminderRequest req) {
        log.info("Creating reminder for user: {}, title: {}", userId, req.getTitle());
        validateRrule(req.getRrule());

        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setTitle(req.getTitle());
        reminder.setMessage(req.getMessage());
        reminder.setRrule(req.getRrule());
        reminder.setStartTime(req.getStartTime());
        reminder.setNextRunTime(req.getStartTime());
        reminder.setIsActive(true);

        Reminder saved = reminderRepository.save(reminder);
        log.info("Created reminder with id: {} for user: {}", saved.getId(), userId);
        return toResponse(saved);
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
        log.info("Updating reminder: {} for user: {}", id, userId);
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
        log.info("Deleting reminder: {} for user: {}", id, userId);
        Reminder reminder = findOwnedReminder(userId, id);
        reminderRepository.delete(reminder);
        log.info("Deleted reminder: {}", id);
    }

    public List<OffsetDateTime> previewReminder(PreviewReminderRequest req) {
        validateRrule(req.getRrule());
        return computeOccurrences(req.getRrule(), req.getStartTime(), req.getCount());
    }

    @Transactional(readOnly = true)
    public List<Reminder> findDueReminders(OffsetDateTime now) {
        log.debug("Finding due reminders at: {}", now);
        List<Reminder> reminders = reminderRepository.findDueReminders(now);
        log.info("Found {} due reminders", reminders.size());
        return reminders;
    }

    @Transactional
    public void updateNextRunTime(UUID reminderId, OffsetDateTime nextRunTime) {
        log.info("Updating next run time for reminder: {} to: {}", reminderId, nextRunTime);
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFoundException(reminderId));
        reminder.setNextRunTime(nextRunTime);
        reminderRepository.save(reminder);
        log.debug("Updated next run time for reminder: {}", reminderId);
    }

    public OffsetDateTime computeNextOccurrence(String rrule, OffsetDateTime currentTime) {
        log.debug("Computing next occurrence for rrule: {} from: {}", rrule, currentTime);
        if (rrule == null || rrule.isBlank()) {
            log.debug("No rrule provided, returning null");
            return null;
        }
        
        try {
            Recur recur = new Recur(rrule);
            DateTime seed = new DateTime(currentTime.toInstant().toEpochMilli());
            DateTime periodStart = seed;
            DateTime periodEnd = new DateTime(currentTime.plusYears(5).toInstant().toEpochMilli());
            
            DateList dates = recur.getDates(seed, periodStart, periodEnd, Value.DATE_TIME);
            
            OffsetDateTime nextOccurrence = dates.stream()
                    .map(d -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(d.getTime()), ZoneOffset.UTC))
                    .filter(dt -> dt.isAfter(currentTime))
                    .findFirst()
                    .orElse(null);
            log.debug("Computed next occurrence: {}", nextOccurrence);
            return nextOccurrence;
        } catch (Exception e) {
            log.error("Error computing next occurrence for rrule: {}", rrule, e);
            throw new RruleValidationException(rrule, e.getMessage());
        }
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
        if (rrule == null || rrule.isBlank()) {
            return List.of(startTime);
        }
        
        try {
            Recur recur = new Recur(rrule);
            DateTime seed = new DateTime(startTime.toInstant().toEpochMilli());
            DateTime periodStart = seed;
            DateTime periodEnd = new DateTime(startTime.plusYears(5).toInstant().toEpochMilli());
            
            DateList dates = recur.getDates(seed, periodStart, periodEnd, Value.DATE_TIME);
            
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
