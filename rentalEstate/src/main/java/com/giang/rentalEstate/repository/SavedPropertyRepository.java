package com.giang.rentalEstate.repository;

import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.SavedProperty;
import com.giang.rentalEstate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SavedPropertyRepository extends JpaRepository<SavedProperty, Long> {
    List<SavedProperty> findByUser(User user);
    boolean existsByUserAndProperty(User user, Property property);
    void deleteByUserAndProperty(User user, Property property);
    int countByProperty(Property property);
    void deleteByProperty(Property property);
    long countByUser(User user);
    @Query(value = "SELECT DATE(created_at) as date, COUNT(*) as count " +
           "FROM saved_properties " +
           "WHERE created_at >= :startDate " +
           "GROUP BY DATE(created_at) " +
           "ORDER BY date", nativeQuery = true)
    List<Object[]> countSavedByDay(@Param("startDate") LocalDateTime startDate);

    @Query(value = "SELECT DATE(sp.created_at) as date, COUNT(*) as count " +
           "FROM saved_property sp " +
           "JOIN property p ON sp.property_id = p.id " +
           "WHERE p.owner_id = :ownerId " +
           "AND sp.created_at >= :startDate " +
           "GROUP BY DATE(sp.created_at) " +
           "ORDER BY date", nativeQuery = true)
    List<Object[]> countSavedByDayForOwner(@Param("ownerId") Long ownerId, @Param("startDate") LocalDateTime startDate);

    void deleteByUser(User user);
}
