package com.giang.rentalEstate.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.giang.rentalEstate.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "property")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="title")
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name="city")
    @JsonIgnore
    private String city;
    @Column(name="district")
    @JsonIgnore
    private String district;
    @Column(name="ward")
    @JsonIgnore
    private String ward;
    @Column(name="street")
    private String street;
    @Column(name="address")
    private String address;
    @Column(name = "area")
    private double area;
    @Column(name="price_per_month")
    private double pricePerMonth;
    @Column(name="num_of_bedrooms")
    private int bedrooms;
    @Column(name="num_of_bathrooms")
    private int bathrooms;
    @Column(name="num_of_floors")
    private int floors;
//    @Column(name="max_tenants")
    private Integer maxTenants = 6; //giới hạn tối đa 6 lượt đăng ký thuê
    @Column(name="current_tenants")
    private Integer currentTenants = 0; //số lượng người đã đăng ký
    @Column(name = "display_until")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonIgnore
    private LocalDateTime displayUntil;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RentalRequest> rentalRequests = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonBackReference
    private User customer; //người thuê được chọn
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_status")
    private PropertyStatus status = PropertyStatus.PENDING_REVIEW; //mặc định chờ thanh toán
    @Enumerated(EnumType.STRING)
    @Column(name = "property_type")
    private PropertyType type;
    @Enumerated(EnumType.STRING)
    @Column(name = "furniture_status")
    private FurnitureStatus furnitureStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "house_direction")
    private Direction direction;
    @Enumerated(EnumType.STRING)
    @Column(name = "move_in_time")
    private MoveInTime moveInTime;
    @Enumerated(EnumType.STRING)
    @Column(name = "electricity_price")
    private PricingOption electricityPrice;
    @Enumerated(EnumType.STRING)
    @Column(name = "water_price")
    private PricingOption waterPrice;
    @Enumerated(EnumType.STRING)
    @Column(name = "internet_price")
    private PricingOption internetPrice;
    @Column(name = "saved_count")
    private Integer savedCount = 0;

    @ElementCollection
    @CollectionTable(name = "property_amenities", joinColumns = @JoinColumn(name = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "amenity")
    private List<Amenity> amenities;
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PropertyImage> images = new ArrayList<>();
    @CreationTimestamp
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
//    @JsonIgnore
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
//    @JsonIgnore
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;
    private String rejectReason;



    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
