package com.reminder.reminder_service.repository;

import com.reminder.reminder_service.entity.ExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExecutionLogRepository extends JpaRepository<ExecutionLog, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
