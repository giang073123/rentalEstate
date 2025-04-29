package com.giang.rentalEstate.repository;

import com.giang.rentalEstate.enums.RentalRequestStatus;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.RentalRequest;
import com.giang.rentalEstate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequest, Long> {
    boolean existsByPropertyAndCustomer(Property property, User customer);
    Optional<RentalRequest> findByPropertyAndCustomer(Property property, User customer);
    List<RentalRequest> findByProperty(Property property);
    List<RentalRequest> findByPropertyIn(List<Property> properties);
    List<RentalRequest> findByCustomer(User customer);
    // Đếm số lượng yêu cầu thuê đang hoạt động (đã được chọn hoặc đã được duyệt)
    long countByCustomerAndStatusIn(User user, List<RentalRequestStatus> statuses);

    // Đếm số lượng yêu cầu thuê đang chờ xử lý
    long countByCustomerAndStatus(User user, RentalRequestStatus status);
    long countByPropertyIdInAndStatus(List<Long> ids, RentalRequestStatus status);
    void deleteByPropertyId(Long id);
    long countByStatus(RentalRequestStatus rentalRequestStatus);
    @Query("SELECT r FROM RentalRequest r " +
            "WHERE r.property.owner = :owner AND r.status = :status " +
            "ORDER BY r.createdAt DESC")
    List<RentalRequest> findRecentRequestsForOwner(
            @Param("owner") User owner,
            @Param("status") RentalRequestStatus status
    );
    void deleteByCustomer(User user);
}