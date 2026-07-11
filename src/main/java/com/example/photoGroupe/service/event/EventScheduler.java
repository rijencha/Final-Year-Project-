package com.example.photoGroupe.service.event;

import com.example.photoGroupe.model.event.EventRequestStatus;
import com.example.photoGroupe.repo.EventRequestRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRequestRepository eventRequestRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void closeExpiredEvents() {
        eventRequestRepository
                .findByStatusAndDeadlineAtBefore(EventRequestStatus.OPEN, LocalDateTime.now())
                .forEach(event -> {
                    event.setStatus(EventRequestStatus.CLOSED);
                    notificationService.create(
                            event.getClient(),
                            null,
                            "EVENT_CLOSED",
                            "Your event \"" + event.getTitle() + "\" has closed for bidding",
                            "/events/" + event.getId()
                    );
                });
    }
}