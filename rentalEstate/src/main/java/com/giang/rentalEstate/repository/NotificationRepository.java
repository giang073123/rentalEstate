package com.giang.rentalEstate.repository;

import com.giang.rentalEstate.model.Notification;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, boolean isRead);
    long countByUserAndIsRead(User user, boolean isRead);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.rentalRequest = ?1")
    void deleteByRentalRequest(RentalRequest rentalRequest);
    void deleteByProperty(Property property);
    void deleteByUserId(Long userId);
} 