package com.giang.rentalEstate.service.impl;

import com.giang.rentalEstate.exception.NotificationNotAuthorizedException;
import com.giang.rentalEstate.exception.RentalRequestNotAuthorizedException;
import com.giang.rentalEstate.exception.ResourceNotFoundException;
import com.giang.rentalEstate.model.Notification;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.repository.NotificationRepository;
import com.giang.rentalEstate.repository.PropertyRepository;
import com.giang.rentalEstate.repository.RentalRequestRepository;
import com.giang.rentalEstate.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final PropertyRepository propertyRepository;
    private final RentalRequestRepository rentalRequestRepository;

    @Override
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);
    }

    @Override
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByUserAndIsRead(user, false);
    }

    @Override
    public Notification markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Thông báo không tồn tại"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new NotificationNotAuthorizedException("Bạn không có quyền đánh dấu đã đọc cho thông báo này");
        }

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    public void createNotification(String title, String content, User user, Long propertyId, Long rentalRequestId) {
        Property property = null;
        if (propertyId != null) {
            property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new ResourceNotFoundException("BĐS không tồn tại"));
        }

        RentalRequest rentalRequest = null;
        if (rentalRequestId != null) {
            rentalRequest = rentalRequestRepository.findById(rentalRequestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Yêu cầu thuê không tồn tại"));
        }

        System.out.println("Rental request with id: " + rentalRequest);
        Notification notification = Notification.builder()
                .title(title)
                .content(content)
                .isRead(false)
                .user(user)
                .property(property)
                .rentalRequest(rentalRequest)
                .build();

        notificationRepository.save(notification);
    }
} 