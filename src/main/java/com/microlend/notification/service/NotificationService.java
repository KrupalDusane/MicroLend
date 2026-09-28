package com.microlend.notification.service;

import com.microlend.notification.dto.NotificationResponse;
import com.microlend.notification.dto.UnreadCountResponse;
import com.microlend.notification.entity.Notification;
import com.microlend.notification.entity.NotificationType;
import com.microlend.notification.repository.NotificationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void createNotification(User user, NotificationType type, String title, String message, String relatedEntityType, Long relatedEntityId) {
        if (notificationRepository.existsByUserAndNotificationTypeAndRelatedEntityTypeAndRelatedEntityId(user, type, relatedEntityType, relatedEntityId)) {
            return;
        }

        Notification notification = new Notification(user, type, title, message, relatedEntityType, relatedEntityId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return notificationRepository.findByUserOrderByCreatedAtDescIdDesc(user, pageable)
                .map(NotificationResponse::new);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadUserNotifications(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDescIdDesc(user, pageable)
                .map(NotificationResponse::new);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(String userEmail) {
        User user = getUserByEmail(userEmail);
        long count = notificationRepository.countByUserAndIsReadFalse(user);
        return new UnreadCountResponse(count);
    }

    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new com.microlend.common.exception.NotificationNotFoundException("Notification not found"));
        
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new com.microlend.common.exception.NotificationNotFoundException("Notification not found"); // Map to 404 for security
        }
        
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = getUserByEmail(userEmail);
        notificationRepository.markAllAsReadForUser(user, LocalDateTime.now());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
