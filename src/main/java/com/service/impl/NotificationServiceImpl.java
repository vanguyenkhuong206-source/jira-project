package com.service.impl;

import com.entity.Notification;
import com.entity.User;
import com.repository.NotificationRepository;
import com.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(User user, String title, String message, String type) {
        Notification n = new Notification();
        n.setUser(user);       // ✅ 
        n.setTitle(title);     // ✅
        n.setMessage(message); // ✅
        n.setType(type);       // ✅
        n.setIsRead(false);    // ✅
        return notificationRepository.save(n);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true); // ✅
            notificationRepository.save(n);
        });
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> list = notificationRepository.findByUserIdAndIsReadFalse(userId);
        list.forEach(n -> n.setIsRead(true)); // ✅
        notificationRepository.saveAll(list);
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}