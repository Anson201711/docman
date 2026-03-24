package com.docman.subscription.service.impl;

import com.docman.subscription.entity.Notification;
import com.docman.subscription.entity.Subscription;
import com.docman.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final Map<Long, Subscription> subscriptionStore = new ConcurrentHashMap<>();
    private final Map<Long, Notification> notificationStore = new ConcurrentHashMap<>();
    private final AtomicLong subscriptionIdCounter = new AtomicLong(1);
    private final AtomicLong notificationIdCounter = new AtomicLong(1);

    @Override
    public Subscription subscribe(Subscription subscription) {
        subscription.setId(subscriptionIdCounter.getAndIncrement());
        subscription.setStatus("ACTIVE");
        subscription.setCreateTime(LocalDateTime.now());
        subscription.setUpdateTime(LocalDateTime.now());
        subscriptionStore.put(subscription.getId(), subscription);
        log.info("User {} subscribed to document {} for events: {}",
                subscription.getUserId(), subscription.getDocumentId(), subscription.getEventType());
        return subscription;
    }

    @Override
    public void unsubscribe(Long id) {
        Subscription subscription = subscriptionStore.get(id);
        if (subscription != null) {
            subscription.setStatus("INACTIVE");
            subscription.setUpdateTime(LocalDateTime.now());
            log.info("User {} unsubscribed from document {}", subscription.getUserId(), subscription.getDocumentId());
        }
    }

    @Override
    public Subscription getSubscription(Long id) {
        return subscriptionStore.get(id);
    }

    @Override
    public List<Subscription> listSubscriptions(Long userId) {
        return subscriptionStore.values().stream()
                .filter(s -> userId.equals(s.getUserId()) && "ACTIVE".equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Subscription> listByDocument(Long documentId) {
        return subscriptionStore.values().stream()
                .filter(s -> documentId.equals(s.getDocumentId()) && "ACTIVE".equals(s.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public void sendNotification(Notification notification) {
        notification.setId(notificationIdCounter.getAndIncrement());
        notification.setStatus("SENT");
        notification.setCreateTime(LocalDateTime.now());
        notificationStore.put(notification.getId(), notification);
        log.info("Sent station notification to user {}: {}", notification.getUserId(), notification.getTitle());
    }

    @Override
    public void sendEmailNotification(Long userId, String subject, String content) {
        log.info("Sending email notification to user {}: {} - {}", userId, subject, content);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationStore.values().stream()
                .filter(n -> userId.equals(n.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationStore.get(notificationId);
        if (notification != null) {
            notification.setReadStatus("READ");
            notification.setReadTime(LocalDateTime.now());
        }
    }
}
