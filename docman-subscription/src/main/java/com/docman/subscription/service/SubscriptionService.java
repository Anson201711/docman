package com.docman.subscription.service;

import com.docman.subscription.entity.Notification;
import com.docman.subscription.entity.Subscription;

import java.util.List;

public interface SubscriptionService {
    Subscription subscribe(Subscription subscription);
    void unsubscribe(Long id);
    Subscription getSubscription(Long id);
    List<Subscription> listSubscriptions(Long userId);
    List<Subscription> listByDocument(Long documentId);

    void sendNotification(Notification notification);
    void sendEmailNotification(Long userId, String subject, String content);
    List<Notification> getUserNotifications(Long userId);
    void markAsRead(Long notificationId);
}
