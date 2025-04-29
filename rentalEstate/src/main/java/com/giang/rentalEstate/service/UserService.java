package com.giang.rentalEstate.service;

import com.giang.rentalEstate.dto.*;
import com.giang.rentalEstate.model.Role;
import com.giang.rentalEstate.model.User;

import java.util.List;

public interface UserService {
    String registerUser(UserSignupDTO userSignupDTO);
    void verifyEmail(String token);
    User getUserById(Long id);
    List<UserDTO> getAllUsers();
    void deleteUser(Long id);
    User updateUser(Long id, UserUpdateDTO user);
    TokenResponseDTO login(String username, String password);
    User updatePassword(User currentUser, UserChangePwdDTO userChangePwdDTO);
    String refreshAccessToken(String refreshToken);
    void logout(User user);
    void resendVerificationEmail(String email);
    void sendPasswordResetEmail(String email);
    void resetPassword(String token, UserResetPwdDTO userResetPwdDTO);
//    void createAndSendVerificationToken(String emailUser, User user);
    long getTotalUserCount();
    long countUsersByRole(String roleName);
    long getActiveUserPercentage();
    void updateUserRole(Long id, Role role);
}
