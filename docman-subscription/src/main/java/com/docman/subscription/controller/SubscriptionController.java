package com.docman.subscription.controller;

import com.docman.subscription.entity.Notification;
import com.docman.subscription.entity.Subscription;
import com.docman.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<Subscription> subscribe(@RequestBody Subscription subscription) {
        return ResponseEntity.ok(subscriptionService.subscribe(subscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsubscribe(@PathVariable Long id) {
        subscriptionService.unsubscribe(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Subscription> getSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscription(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Subscription>> listSubscriptions(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.listSubscriptions(userId));
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<Subscription>> listByDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(subscriptionService.listByDocument(documentId));
    }

    @PostMapping("/notification")
    public ResponseEntity<Void> sendNotification(@RequestBody Notification notification) {
        subscriptionService.sendNotification(notification);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email")
    public ResponseEntity<Void> sendEmailNotification(@RequestParam Long userId,
                                                      @RequestParam String subject,
                                                      @RequestParam String content) {
        subscriptionService.sendEmailNotification(userId, subject, content);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long userId) {
        return ResponseEntity.ok(subscriptionService.getUserNotifications(userId));
    }

    @PutMapping("/notifications/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        subscriptionService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }
}
