package com.giang.rentalEstate.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
//    @JsonIgnore
    private Long id;
    @Column(name = "fullname")
    private String fullName;
    @Column(name = "username", unique = true)
    private String username;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email", unique = true)
    private String email;
    @Column(name = "password")
    @JsonIgnore
    private String password;
    @Column(name="tokenversion")
    @JsonIgnore
    private int tokenVersion = 0; // Default value

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private UserToken pwdResetToken;
    @ManyToOne
    @JoinColumn(name="role_id", nullable = false)
    // @JsonBackReference
    private Role role;

    @CreationTimestamp
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonIgnore
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonIgnore
    private LocalDateTime updatedAt;
    @JsonIgnore
    private boolean enabled = false; // Mặc định chưa kích hoạt
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private List<UserToken> tokens = new ArrayList<>();



    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Property> ownerProperties = new ArrayList<>();
    @OneToMany(mappedBy = "customer", cascade = CascadeType.MERGE)
    @JsonManagedReference
    @JsonIgnore
    private List<Property> rentedProperties = new ArrayList<>();
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @JsonIgnore
    private List<RentalRequest> requestList = new ArrayList<>();

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private UserStatus status = UserStatus.ACTIVE;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_"+getRole().getName().name().toUpperCase()));
        return authorityList;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

//    @JsonIgnore
//    @Override
//    public boolean isEnabled() {
//        return UserDetails.super.isEnabled();
//    }

//    public UserStatus getStatus() {
//        return status;
//    }
//
//    public void setStatus(UserStatus status) {
//        this.status = status;
//    }
}
