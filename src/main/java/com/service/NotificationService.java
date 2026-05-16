// NotificationService.java
package com.service;

import com.entity.Notification;
import com.entity.User;
import java.util.List;

public interface NotificationService {
    Notification createNotification(User user, String title, String message, String type);
    List<Notification> getUserNotifications(Long userId);
    List<Notification> getUnreadNotifications(Long userId);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    long countUnread(Long userId);
}