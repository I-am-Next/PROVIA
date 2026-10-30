package com.pm.paymentservicenew.application.service;

import com.pm.paymentservicenew.domain.exception.DuplicateWebhookEventException;
import com.pm.paymentservicenew.domain.model.idempotency.ProcessedEvent;
import com.pm.paymentservicenew.domain.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

@Service
@Transactional
public class IdempotencyServiceImpl implements IdempotencyService {

    private final ProcessedEventRepository repository;


    public IdempotencyServiceImpl(
            ProcessedEventRepository repository
    ) {

        this.repository = repository;

    }

    @Override
    public void registerEvent(

            String provider,

            String idempotencyKey,

            String eventType

    ) {

        try {

            repository.save(

                    ProcessedEvent.create(

                            provider,

                            idempotencyKey,

                            eventType

                    )

            );

        } catch (DataIntegrityViolationException ex) {

            throw new DuplicateWebhookEventException();

        }

    }

}