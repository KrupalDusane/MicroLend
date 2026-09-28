package com.microlend.notification.controller;

import com.microlend.notification.dto.NotificationResponse;
import com.microlend.notification.dto.UnreadCountResponse;
import com.microlend.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        notificationResponse = new NotificationResponse();
        notificationResponse.setId(10L);
        notificationResponse.setTitle("Test Title");
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void getMyNotifications_ReturnsPage() throws Exception {
        Page<NotificationResponse> page = new PageImpl<>(List.of(notificationResponse));
        when(notificationService.getUserNotifications(eq("customer@example.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(10L))
                .andExpect(jsonPath("$.content[0].title").value("Test Title"));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void getUnreadCount_ReturnsCount() throws Exception {
        UnreadCountResponse countResponse = new UnreadCountResponse(5L);
        when(notificationService.getUnreadCount("customer@example.com")).thenReturn(countResponse);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(5L));
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void markAsRead_ReturnsOk() throws Exception {
        doNothing().when(notificationService).markAsRead(10L, "customer@example.com");

        mockMvc.perform(patch("/api/notifications/10/read"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).markAsRead(10L, "customer@example.com");
    }

    @Test
    @WithMockUser(username = "customer@example.com", roles = {"CUSTOMER"})
    void markAllAsRead_ReturnsOk() throws Exception {
        doNothing().when(notificationService).markAllAsRead("customer@example.com");

        mockMvc.perform(patch("/api/notifications/read-all"))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).markAllAsRead("customer@example.com");
    }
    
    @Test
    void getMyNotifications_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }
}
