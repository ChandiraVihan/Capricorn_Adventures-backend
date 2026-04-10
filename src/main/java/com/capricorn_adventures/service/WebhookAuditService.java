package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.PaymentWebhookEvent;
import com.capricorn_adventures.repository.PaymentWebhookEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service dedicated to resiliently logging webhook events.
 * Using REQUIRES_NEW ensures that the audit record is committed 
 * even if the main processing transaction rolls back.
 */
@Service
public class WebhookAuditService {

    private final PaymentWebhookEventRepository webhookRepo;

    public WebhookAuditService(PaymentWebhookEventRepository webhookRepo) {
        this.webhookRepo = webhookRepo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentWebhookEvent saveInitialAudit(String eventId, String statusCode, String payload) {
        PaymentWebhookEvent record = new PaymentWebhookEvent();
        record.setEventId(eventId);
        record.setEventType("PAYHERE_STATUS_" + statusCode);
        record.setPayload(payload);
        record.setStatus("RECEIVED");
        record.setReceivedAt(LocalDateTime.now());
        return webhookRepo.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEventStatus(PaymentWebhookEvent record, String status) {
        record.setStatus(status);
        record.setProcessedAt(LocalDateTime.now());
        webhookRepo.save(record);
    }
}
