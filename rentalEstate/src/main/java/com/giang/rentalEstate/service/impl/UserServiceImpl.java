package com.giang.rentalEstate.service.impl;

import com.giang.rentalEstate.converter.UserConverter;
import com.giang.rentalEstate.dto.*;
import com.giang.rentalEstate.enums.Rolename;
import com.giang.rentalEstate.enums.TokenType;
import com.giang.rentalEstate.exception.AdminDeletionNotAllowedException;
import com.giang.rentalEstate.exception.InvalidTokenException;
import com.giang.rentalEstate.exception.ResendLimitExceededException;
import com.giang.rentalEstate.exception.ResourceNotFoundException;
import com.giang.rentalEstate.exception.UserAlreadyVerifiedException;
import com.giang.rentalEstate.exception.UserNotVerifiedException;
import com.giang.rentalEstate.model.*;
import com.giang.rentalEstate.repository.*;
import com.giang.rentalEstate.service.EmailService;
import com.giang.rentalEstate.service.UserService;
import com.giang.rentalEstate.service.UserTokenService;
import com.giang.rentalEstate.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserTokenRepository userTokenRepository;
    private final NotificationRepository notificationRepository;
    private final RentalRequestRepository rentalRequestRepository;
    private final PropertyRepository propertyRepository;
    private final SavedPropertyRepository savedPropertyRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final UserConverter userConverter;
    private final UserTokenService userTokenService;
    private static final long EXPIRATION_TIME_IN_MINUTES = 1; // Token expires in 30 mins
    @Override
    @Transactional
    public String registerUser(UserSignupDTO userSignupDTO) {
        String username = userSignupDTO.getUsername();
        String email = userSignupDTO.getEmail();
        //Kiểm tra xem username và email này đã tồn tại hay chưa
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        System.out.println("existingUserOpt: " + existingUserOpt);

        if (existingUserOpt.isPresent()) { //neu ton tai user co email nay
            //neu user nay chua xac thuc, token van con han => loi email exists
            //neu user chua xac thuc, token het han => xoa user va token => user moi co the dang ky trung
            //neu user xac thuc => loi email exists
            User existingUser = existingUserOpt.get();
            if(existingUser.isEnabled()){
                throw new DataIntegrityViolationException("Email đã tồn tại");
            }
            Optional<UserToken> userTokenOpt = userTokenRepository.findByUserAndTokenType(existingUser, TokenType.EMAIL_VERIFICATION);
            if(userTokenOpt.isPresent()) { //neu co token trong csdl
                if(!userTokenOpt.get().getExpiryDate().isBefore(LocalDateTime.now())){ //neu token khong het han
                    throw new DataIntegrityViolationException("Email đã tồn tại");
                }
                userRepository.delete(existingUser);
                userTokenRepository.deleteByToken(userTokenOpt.get().getToken());
            }

        }

        if(userRepository.existsByUsername(username)){
            throw new DataIntegrityViolationException("Username đã tồn tại");
        }
        Role userRole = roleRepository.findById(userSignupDTO.getRoleid())
                .orElseThrow(() -> new ResourceNotFoundException("Vai trò này không tồn tại"));
        //convert từ UserSignupDTO sang User (entity)
        User newUser = User.builder()
                .fullName(userSignupDTO.getFullName())
                .username(userSignupDTO.getUsername())
                .phone(userSignupDTO.getPhone())
                .email(userSignupDTO.getEmail())
                .password(bCryptPasswordEncoder.encode(userSignupDTO.getPassword()))
                .role(userRole)
                .enabled(false)
                .build();
        System.out.println("newUser: " + newUser);
        userRepository.save(newUser);
        userTokenService.createAndSendVerificationToken(email, newUser);
        System.out.println("email dong 91: " + email);
        return email;
    }
    @Override
    @Transactional
    public void verifyEmail(String token) {
        Optional<UserToken> verificationTokenOpt = userTokenRepository.findByToken(token);
        if(verificationTokenOpt.isPresent()) {
            UserToken verificationToken = verificationTokenOpt.get();
            User user = verificationToken.getUser();
            if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) { // nếu token hết hạn
                if (user.isEnabled()) { //nếu token hết hạn và người dùng đã xác thực
                    throw new UserAlreadyVerifiedException("Người dùng đã xác thực.");
                }
                //nếu token hết hạn và người dùng chưa xác thực: tiến hành xóa user trong Entity user, xóa token trong Entity usertoken.
                //và yêu cầu đăng ký mới
                userRepository.delete(user);
                userTokenRepository.deleteByToken(verificationToken.getToken());
                throw new InvalidTokenException("Token không hợp lệ. Vui lòng yêu cầu đăng ký mới.");
            }
            //neu token con han:
            if (user.isEnabled()) { //da xac thuc
                throw new UserAlreadyVerifiedException("Người dùng đã xác thực.");
            }
            //chua xac thuc:
            user.setEnabled(true);
            userRepository.save(user);
            verificationToken.setResendCount(0);
            userTokenRepository.deleteByToken(verificationToken.getToken());
        } else {
            throw new InvalidTokenException("Token không hợp lệ. Vui lòng yêu cầu đăng ký mới.");
        }
    }

//    @Scheduled(fixedRate = 1800000) // Runs every 30 minutes
    @Scheduled(fixedRate = 120000) // Runs every 1 minutes
    @Transactional
    public void deleteExpiredTokensAndUnverifiedUsers() {
        System.out.println("SCHEDULE START: " + LocalDateTime.now());
        List<UserToken> expiredTokens = userTokenRepository.findByExpiryDateBeforeAndTokenType(
                LocalDateTime.now(), TokenType.EMAIL_VERIFICATION
        );
        Set<User> usersToDelete = new HashSet<>();
        for(UserToken token : expiredTokens){
            User user = token.getUser();
            userTokenRepository.deleteByToken(token.getToken());
            if(!user.isEnabled() && !userTokenRepository.existsByUserAndTokenType(user, TokenType.EMAIL_VERIFICATION)){ //trong số token hết hạn, token nào ứng với user chua xác thực
                usersToDelete.add(user);
            }
        }
        userRepository.deleteAll(usersToDelete);
        System.out.println("SCHEDULE END: " + LocalDateTime.now());
    }
    // /**
    //  * This method resends verification email to the user
    //  * @param email email of the user
    //  * @throws UserException if the user is not found or already verified
    //  */
    // @Operation(
    //     summary = "Resend verification email",
    //     description = "Resend verification email to the user"
    // )
    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại."));
        if (user.isEnabled()) {
            throw new UserAlreadyVerifiedException("Người dùng đã xác thực.");
        }
        Optional<UserToken> existingTokenOpt = userTokenRepository.findByUserAndTokenType(user, TokenType.EMAIL_VERIFICATION);
        // Nếu có token cũ trong csdl
        if (existingTokenOpt.isPresent()) {
            UserToken existingToken = existingTokenOpt.get();
            if (existingToken.getExpiryDate().isBefore(LocalDateTime.now())) {//neu token het han
                userTokenRepository.deleteByToken(existingToken.getToken());
                userRepository.delete(user);
                throw new InvalidTokenException("Token không hợp lệ. Vui lòng yêu cầu đăng ký mới.");
            }
            //neu token con han, kiem tra gioi han so lan gui lai
            if (existingToken.getResendCount() >= 3) {
                throw new ResendLimitExceededException("Bạn đã đạt giới hạn gửi lại. Vui lòng thử lại sau.");
            }
            //Nếu số lần gửi lại chưa quá giới hạn
            // => tăng số lần gửi lại và cập nhật thời gian
            existingToken.setResendCount(existingToken.getResendCount() + 1);
            String randomToken = UUID.randomUUID().toString();
            existingToken.setToken(randomToken);
            existingToken.setExpiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES));
            String verifyLink = "http://localhost:3000/verify-email?token=" + randomToken;
            String message = "Chúng tôi cần xác nhận địa chỉ email của bạn vẫn còn hợp lệ. Vui lòng nhấp vào liên kết bên dưới để xác nhận rằng bạn đã nhận được email này:\n" + verifyLink;
            emailService.sendEmail(user.getEmail(), "Vui lòng xác nhận địa chỉ email của bạn", message);
            userTokenRepository.save(existingToken);
        } else { //neu khong co token cu
            userTokenService.createAndSendVerificationToken(email, user);
        }
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> otp = userRepository.findById(id);
        if(otp.isPresent()){
            return otp.get();
        }
        throw new ResourceNotFoundException("Người dùng không tồn tại");
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<UserDTO> userDTOS = userRepository.findAll().stream().map(userConverter::toUserDTO).toList();
        return userDTOS;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if(userOpt.isEmpty()){
            throw new ResourceNotFoundException("Người dùng không tồn tại");
        }
        
        User user = userOpt.get();
        // Kiểm tra nếu là admin
        if(user.getRole().getName() == Rolename.ADMIN) {
            throw new AdminDeletionNotAllowedException("Không thể xóa tài khoản admin");
        }

        // Xóa các thông báo liên quan đến rental request của user
        List<RentalRequest> rentalRequests = rentalRequestRepository.findByCustomer(user);
        for (RentalRequest request : rentalRequests) {
            notificationRepository.deleteByRentalRequest(request);
        }

        // Xóa các thông báo liên quan đến property của user
        List<Property> properties = propertyRepository.findByOwner(user);
        for (Property property : properties) {
            notificationRepository.deleteByProperty(property);
        }

        // Xóa các thông báo trực tiếp của user
        notificationRepository.deleteByUserId(id);
        
        // Xóa các token của user
        userTokenRepository.deleteByUser(user);
        
        // Xóa các saved property của user
        savedPropertyRepository.deleteByUser(user);
        
        if(user.getRole().getName() == Rolename.CUSTOMER){  
            // Xóa các rental request của user
            rentalRequestRepository.deleteByCustomer(user);
        }

        if(user.getRole().getName() == Rolename.OWNER){
            // Xóa các saved property liên quan đến property của user
            for (Property property : properties) {
                savedPropertyRepository.deleteByProperty(property);
            }
            // Xóa các property của user
            propertyRepository.deleteByOwner(user);
        }
        
        // Xóa user
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        existingUser.setFullName(userUpdateDTO.getFullName());
        existingUser.setPhone(userUpdateDTO.getPhone());
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public TokenResponseDTO login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isEmpty()){
            throw new BadCredentialsException("Username hoặc mật khẩu không chính xác");
        }
        User existingUser = optionalUser.get();
        if(!passwordEncoder.matches(password, existingUser.getPassword())) {
            throw new BadCredentialsException("Username hoặc mật khẩu không chính xác");
        }
        if(!existingUser.isEnabled()){ //chua verify
            Optional<UserToken> existingTokenOpt = userTokenRepository.findByUserAndTokenType(existingUser, TokenType.EMAIL_VERIFICATION);
            // Nếu có token cũ trong csdl => xóa luôn để gửi cái mới
            if (existingTokenOpt.isPresent()) {
                UserToken existingToken = existingTokenOpt.get();
                userTokenRepository.deleteByToken(existingToken.getToken());
                userRepository.delete(existingUser);
            }
            userTokenService.createAndSendVerificationToken(existingUser.getEmail(), existingUser);
            throw new UserNotVerifiedException("Người dùng chưa xác thực. Vui lòng kiểm tra email để xác thực tài khoản.");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password, existingUser.getAuthorities()));
        existingUser.setTokenVersion(existingUser.getTokenVersion() + 1);
        userRepository.save(existingUser);
        String accessToken = jwtTokenUtil.generateTokenFromUsername(existingUser);
        String refreshToken = jwtTokenUtil.generateRefreshToken(existingUser);
        return new TokenResponseDTO(accessToken, refreshToken);
//        return existingUser;
    }

    @Override
    @Transactional
    public String refreshAccessToken(String refreshToken) {
        String username = jwtTokenUtil.getUserNameFromJwtToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        if(!jwtTokenUtil.validateToken(refreshToken, user)){
            throw new InvalidTokenException("Token không hợp lệ");
        }
        return jwtTokenUtil.generateTokenFromUsername(user);
    }

    @Override
    @Transactional
    public void logout(User user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User updatePassword(User currentUser, UserChangePwdDTO userChangePwdDTO) {
        currentUser.setPassword(bCryptPasswordEncoder.encode(userChangePwdDTO.getNewPassword()));
//        currentUser.setTokenVersion(currentUser.getTokenVersion() + 1);
        return userRepository.save(currentUser);
    }
    @Override
    @Transactional
    public void sendPasswordResetEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalUser.isEmpty()){
            throw new ResourceNotFoundException("Người dùng không tồn tại");
        }
        User user = optionalUser.get();
        //chua verify -> van reset pwd duoc -> den luc login thi se bat verify bang cach gui email verification
        Optional<UserToken> existingTokenOpt = userTokenRepository.findByUserAndTokenType(user, TokenType.PASSWORD_RESET);
        // Nếu có token cũ trong csdl => xóa luôn để gửi cái mới
        if (existingTokenOpt.isPresent()) {
            UserToken existingToken = existingTokenOpt.get();
            userTokenRepository.deleteByToken(existingToken.getToken());
        }
        String token = UUID.randomUUID().toString();
        UserToken resetToken = UserToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES))
                .tokenType(TokenType.PASSWORD_RESET)
                .build();
        userTokenRepository.save(resetToken);

        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        String message = "Ai đó đã yêu cầu đặt lại mật khẩu của bạn. Nếu yêu cầu này không phải là bạn, người này có thể đã nhập sai địa chỉ email, và bạn có thể bỏ qua email này. Nếu yêu cầu này là của bạn, vui lòng nhấp vào liên kết bên dưới để đặt lại mật khẩu của bạn:\n" + resetLink;
        emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", message);
    }
    @Override
    @Transactional
    public void resetPassword(String token, UserResetPwdDTO userResetPwdDTO) {
        UserToken pwdResetToken = userTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token không hợp lệ hoặc đã hết hạn"));
        if(pwdResetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            userTokenRepository.deleteByToken(pwdResetToken.getToken());
            throw new InvalidTokenException("Token đã hết hạn");
        }
        User user = userRepository.findById(pwdResetToken.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        user.setPassword(bCryptPasswordEncoder.encode(userResetPwdDTO.getNewPassword()));
        userRepository.save(user);
        userTokenRepository.deleteByToken(pwdResetToken.getToken());
    }

    @Override
    public long getTotalUserCount() {
        return userRepository.count();
    }

    @Override
    public long countUsersByRole(String roleName) {
        return userRepository.countByRole_Name(Rolename.valueOf(roleName));
    }

    @Override
    public long getActiveUserPercentage() {
        long totalUsers = userRepository.count();
        if (totalUsers == 0) return 0;
        
        // Đếm số người dùng có hoạt động trong 4 ngày gần nhất
        LocalDateTime fourDaysAgo = LocalDateTime.now().minusDays(4);
        long activeUsers = userRepository.countByUpdatedAtAfter(fourDaysAgo);
        
        // Tính tỷ lệ phần trăm
        return (activeUsers * 100) / totalUsers;
    }

    @Override
    public void updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        
        user.setRole(role);
        userRepository.save(user);
    }

    // @Override
    // public User updateUserStatus(Long userId, UserStatus status) throws UserException {
    //     User user = userRepository.findById(userId)
    //             .orElseThrow(() -> new UserException("Không tìm thấy người dùng"));
        
    //     user.setStatus(status);
    //     return userRepository.save(user);
    // }

    // @Override
    // public void resetUserPassword(Long userId) throws UserException {
    //     User user = userRepository.findById(userId)
    //             .orElseThrow(() -> new UserException("Không tìm thấy người dùng"));
        
    //     // Đặt mật khẩu mặc định là "123456"
    //     String defaultPassword = "123456";
    //     user.setPassword(passwordEncoder.encode(defaultPassword));
    //     userRepository.save(user);
    // }
}
