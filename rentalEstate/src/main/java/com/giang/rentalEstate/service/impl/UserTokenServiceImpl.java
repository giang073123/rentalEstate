package com.giang.rentalEstate.service.impl;

import com.giang.rentalEstate.dto.UserResetPwdDTO;
import com.giang.rentalEstate.enums.TokenType;
import com.giang.rentalEstate.model.UserToken;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.repository.UserTokenRepository;
import com.giang.rentalEstate.repository.UserRepository;
import com.giang.rentalEstate.service.EmailService;
import com.giang.rentalEstate.service.UserTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl implements UserTokenService {
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private static final long EXPIRATION_TIME_IN_MINUTES = 1; // Token expires in 30 mins



//    @Scheduled(fixedRate = 1800000) // Runs every 30 minutes
//    @Transactional
//    public void deleteExpiredResetPwdTokens() {
//        System.out.println("SCHEDULE START: " + LocalDateTime.now());
//        List<UserToken> expiredTokens = userTokenRepository.findByExpiryDateBeforeAndTokenType(
//                LocalDateTime.now(), TokenType.PASSWORD_RESET
//        );
//        for(UserToken token : expiredTokens){
//            userTokenRepository.delete(token);
//        }
//        System.out.println("SCHEDULE END: " + LocalDateTime.now());
//    }
//    @Override
//    public void resetPassword(String token, UserResetPwdDTO userResetPwdDTO) throws UserException {
//        UserToken pwdResetToken = userTokenRepository.findByToken(token)
//                .orElseThrow(() -> new UserException("Invalid or expired token"));
//        if(pwdResetToken.getExpiryDate().isBefore(LocalDateTime.now())){
//            userTokenRepository.delete(pwdResetToken);
//            throw new UserException("Token has expired");
//        }
//        User user = userRepository.findById(pwdResetToken.getUser().getId())
//                .orElseThrow(() -> new UserException("User not found"));
//        user.setPassword(bCryptPasswordEncoder.encode(userResetPwdDTO.getNewPassword()));
//        userRepository.save(user);
//        userTokenRepository.delete(pwdResetToken);
//    }

    @Override
    public void createAndSendVerificationToken(String emailUser, User user) {
        System.out.println("Gửi email cho email: " + emailUser);
        System.out.println("Gửi email cho user: " + user);

        String token = UUID.randomUUID().toString();
        UserToken verficationToken = UserToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES))
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .build();
        userTokenRepository.save(verficationToken);
        System.out.println("Gửi email với token: " + token);
        String verifyLink = "http://localhost:3000/verify-email?token=" + token;
        System.out.println("Gửi email với link: " + verifyLink);
        String message = "We need to confirm your email address is still valid. Please click the link below to confirm you received this mail.:\n" + verifyLink;
        emailService.sendEmail(emailUser, "Please verify your email address", message);
        System.out.println("Gửi email thành công");
    }
////    @Override
//    public void createAndSendResetPasswordToken(String emailUser, User user) {
//        String token = UUID.randomUUID().toString();
//        UserToken resetPasswordToken = UserToken.builder()
//                .user(user)
//                .token(token)
//                .expiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES))
//                .tokenType(TokenType.PASSWORD_RESET)
//                .build();
//        userTokenRepository.save(resetPasswordToken);
//        String verifyLink = "http://localhost:3000/reset-password?token=" + token;
//        String message = "We need to confirm your email address is still valid. Please click the link below to confirm you received this mail.:\n" + verifyLink;
//        emailService.sendEmail(emailUser, "Please verify your email address", message);
//    }


}
