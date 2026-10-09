package com.etp.ticketservice.payments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedWebhookEventServiceImplTest {

    @Mock
    private ProcessedWebhookEventRepository processedWebhookEventRepository;

    @InjectMocks
    private ProcessedWebhookEventServiceImpl processedWebhookEventService;

    @Test
    void recordIfNew_cleanSave_returnsTrue() {
        when(processedWebhookEventRepository.saveAndFlush(any(ProcessedWebhookEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = processedWebhookEventService.recordIfNew(PaymentProvider.STRIPE, "evt_123", "checkout.session.completed");

        assertThat(result).isTrue();
    }

    @Test
    void recordIfNew_uniqueConstraintViolation_returnsFalse() {
        when(processedWebhookEventRepository.saveAndFlush(any(ProcessedWebhookEvent.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        boolean result = processedWebhookEventService.recordIfNew(PaymentProvider.STRIPE, "evt_123", "checkout.session.completed");

        assertThat(result).isFalse();
    }
}
