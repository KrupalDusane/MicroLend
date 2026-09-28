package com.microlend.notification.repository;

import com.microlend.notification.entity.Notification;
import com.microlend.notification.entity.NotificationType;
import com.microlend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserOrderByCreatedAtDescIdDesc(User user, Pageable pageable);

    Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDescIdDesc(User user, Pageable pageable);

    long countByUserAndIsReadFalse(User user);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    boolean existsByUserAndNotificationTypeAndRelatedEntityTypeAndRelatedEntityId(User user, NotificationType type, String relatedEntityType, Long relatedEntityId);
}
