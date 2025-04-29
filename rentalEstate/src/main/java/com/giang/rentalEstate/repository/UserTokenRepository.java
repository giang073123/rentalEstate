package com.giang.rentalEstate.repository;

import com.giang.rentalEstate.enums.TokenType;
import com.giang.rentalEstate.model.UserToken;
import com.giang.rentalEstate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByToken(String token);
    Optional<UserToken> findByUser(User user);
    void deleteAllByUser(User user);
    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
    List<UserToken> findByExpiryDateBeforeAndTokenType(LocalDateTime expiryDate, TokenType tokenType);
    List<UserToken> findByExpiryDateBefore(LocalDateTime expiryDate);
    @Transactional
    @Modifying
    @Query("DELETE FROM UserToken ut WHERE ut.token = :token")
    void deleteByToken(@Param("token") String token);
    Optional<UserToken> findByUserAndTokenType(User user, TokenType tokenType);
    boolean existsByUserAndTokenType(User user, TokenType tokenType);
    void deleteByUser(User user);
}
