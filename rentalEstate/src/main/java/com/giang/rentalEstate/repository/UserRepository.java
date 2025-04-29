package com.giang.rentalEstate.repository;

import com.giang.rentalEstate.enums.Rolename;
import com.giang.rentalEstate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findByRole_Name(Rolename rolename);
    long countByRole_Name(Rolename rolename);

    long countByUpdatedAtAfter(LocalDateTime date);
}
