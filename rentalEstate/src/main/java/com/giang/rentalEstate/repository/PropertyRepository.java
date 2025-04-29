package com.giang.rentalEstate.repository;

import com.giang.rentalEstate.dto.PropertyFilterDTO;
import com.giang.rentalEstate.enums.PropertyStatus;
import com.giang.rentalEstate.model.Property;
import com.giang.rentalEstate.model.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwner(User owner);
    List<Property> findByOwnerId(Long ownerId);
    List<Property> findByStatus(PropertyStatus status);
    boolean existsById(Long id);
    void deleteByOwner(User owner);

    //further reference: https://spring.io/blog/2014/07/15/spel-support-in-spring-data-jpa-query-definitions
    @Query("SELECT p FROM Property p WHERE " +
        "(:#{#filter.type} IS NULL OR :#{#filter.type.size()} = 0 OR p.type IN :#{#filter.type}) AND " +
        "(" +
            "(:#{#filter.city.size()} > 0 AND p.city IN :#{#filter.city}) OR " +
            "(:#{#filter.district.size()} > 0 AND p.district IN :#{#filter.district}) OR " +
            "(:#{#filter.ward.size()} > 0 AND p.ward IN :#{#filter.ward})" +
        ") AND " +
        "(:#{#filter.direction} IS NULL OR :#{#filter.direction.size()} = 0 OR p.direction IN :#{#filter.direction}) AND " +
        "(:#{#filter.bedrooms} IS NULL OR :#{#filter.bedrooms.size()} = 0 OR p.bedrooms IN :#{#filter.bedrooms}) AND " +
        "(:#{#filter.electricityPrice} IS NULL OR p.electricityPrice = :#{#filter.electricityPrice}) AND " +
        "(:#{#filter.waterPrice} IS NULL OR p.waterPrice = :#{#filter.waterPrice}) AND " +
        "(:#{#filter.internetPrice} IS NULL OR p.internetPrice = :#{#filter.internetPrice}) AND " +
        "(:#{#filter.moveInTime} IS NULL OR p.moveInTime = :#{#filter.moveInTime}) AND " +
        "(:minArea IS NULL OR p.area >= :minArea) AND " +
        "(:maxArea IS NULL OR p.area <= :maxArea) AND " +
        "(:minPrice IS NULL OR p.pricePerMonth >= :minPrice) AND " +
        "(:maxPrice IS NULL OR p.pricePerMonth <= :maxPrice) AND " +
        "p.status = :status")
    List<Property> findByFilter(
        @Param("filter") PropertyFilterDTO filter,
        @Param("minArea") Double minArea,
        @Param("maxArea") Double maxArea,
        @Param("minPrice") Double minPrice,
        @Param("maxPrice") Double maxPrice,
        @Param("status") PropertyStatus status
    );
    long countByOwnerAndStatusIn(User user, List<PropertyStatus> propertyStatus);
    long countByOwner(User user);
    long countByStatus(PropertyStatus propertyStatus);
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "0")})
   @Query("SELECT p FROM Property p WHERE p.id = :id")
   Optional<Property> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT p FROM Property p " +
            "LEFT JOIN FETCH p.owner " +
            "LEFT JOIN FETCH p.reviewedBy " +
            "WHERE p.id = :id")
    Optional<Property> findByIdWithDetails(@Param("id") Long id);

//    @Query(value = "SELECT p FROM Property p WHERE MATCH(p.title, p.description, p.address) AGAINST(:keyword IN NATURAL LANGUAGE MODE) AND p.status = :status")
//    List<Property> searchByKeyword(@Param("keyword") String keyword, @Param("status") PropertyStatus status);

    @Query(value = "SELECT * FROM property p WHERE MATCH(p.title, p.description, p.address) AGAINST(:keyword IN BOOLEAN MODE) AND p.property_status = 'APPROVED'", nativeQuery = true)
    List<Property> searchByKeyword(@Param("keyword") String keyword);
}
