package com.microlend.notification.service;

import com.microlend.notification.dto.NotificationResponse;
import com.microlend.notification.dto.UnreadCountResponse;
import com.microlend.notification.entity.Notification;
import com.microlend.notification.entity.NotificationType;
import com.microlend.notification.repository.NotificationRepository;
import com.microlend.user.entity.User;
import com.microlend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@test.com", null)
        );

        notification = new Notification(
                user,
                NotificationType.LOAN_APPLICATION_SUBMITTED,
                "Test Title",
                "Test Message",
                "LOAN_APPLICATION",
                100L
        );
        notification.setId(10L);
        notification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testGetMyNotifications() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(notification));
        when(notificationRepository.findByUserOrderByCreatedAtDescIdDesc(user, pageable)).thenReturn(page);

        Page<NotificationResponse> result = notificationService.getUserNotifications("test@test.com", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetUnreadCount() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserAndIsReadFalse(user)).thenReturn(5L);

        UnreadCountResponse response = notificationService.getUnreadCount("test@test.com");

        assertNotNull(response);
        assertEquals(5L, response.getUnreadCount());
    }

    @Test
    void testMarkAsRead_Success() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));
        
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        notificationService.markAsRead(10L, "test@test.com");

        assertTrue(notification.isRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    void testMarkAsRead_NotFound() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(com.microlend.common.exception.NotificationNotFoundException.class, () -> notificationService.markAsRead(99L, "test@test.com"));
    }

    @Test
    void testMarkAllAsRead() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        notificationService.markAllAsRead("test@test.com");

        verify(notificationRepository, times(1)).markAllAsReadForUser(eq(user), any(LocalDateTime.class));
    }
}
