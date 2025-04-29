package com.giang.rentalEstate.service;

import com.giang.rentalEstate.model.Notification;
import com.giang.rentalEstate.model.User;

import java.util.List;

public interface NotificationService {
    List<Notification> getUserNotifications(User user);
    List<Notification> getUnreadNotifications(User user);
    long getUnreadNotificationCount(User user);
    Notification markAsRead(Long notificationId, User user);
    void createNotification(String title, String content, User user, Long propertyId, Long rentalRequestId);
} 