package com.giang.rentalEstate.controller;

import com.giang.rentalEstate.dto.*;
import com.giang.rentalEstate.model.Role;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This controller handles all the requests which are related to user
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true") // Cho phép React frontend truy cập
@Tag(name = "User Controller", description = "APIs for managing user within the system")

public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * This endpoint handles user registration requests
     * @param userSignupDTO This DTO contains registration information
     * @param result Validation result
     * @return ResponseEntity returns contains email if the user signs up successfully. Otherwise, it returns list of errors
     */
    @Operation(
        summary = "Register new user",
        description = "Sign up a new user. Returns email if successful, returns list of errors if failed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Register successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{ \"email\": \"test@example.com\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "[\"Mật khẩu không khớp\"]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                    "  \"error\": \"Resource not found\",\n" +
                    "  \"message\": \"Vai trò này không tồn tại\",\n" +
                    "  \"path\": \"/api/auth/register\",\n" +
                    "  \"status\": 404\n" +
                    "}"
                )   
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Data already exists",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Email exists",
                        value = "{\n" +
                            "  \"error\": \"Data Integrity Violation Error\",\n" +
                            "  \"message\": \"Email đã tồn tại\",\n" +
                            "  \"path\": \"/api/auth/register\",\n" +
                            "  \"status\": 409\n" +
                            "}"
                    ),
                    @ExampleObject(
                        name = "Username exists",
                        value = "{\n" +
                            "  \"error\": \"Data Integrity Violation Error\",\n" +
                            "  \"message\": \"Username đã tồn tại\",\n" +
                            "  \"path\": \"/api/auth/register\",\n" +
                            "  \"status\": 409\n" +
                            "}"
                    )
                }
            )
        )
    })
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserSignupDTO userSignupDTO, BindingResult result) {
            List<String> errorMessages = new ArrayList<>(result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList());

            if(!userSignupDTO.getPassword().equals(userSignupDTO.getRetypePassword())){
                errorMessages.add("Mật khẩu không khớp");
            }
            if(!errorMessages.isEmpty()){
                return ResponseEntity.badRequest().body(errorMessages);

            }
            String email = userService.registerUser(userSignupDTO);//return ResponseEntity.ok("Register successfully");
            return ResponseEntity.ok(Collections.singletonMap("email", email));

    }

    /**
     * This endpoint verifies user email
     * @param token email verification token
     * @return ResponseEntity returns the notification if the user verifies email successfully. Otherwise, it returns list of errors
     */
    @Operation(
        summary = "Verify user email",
        description = "Verify user email. Returns notification if successful, returns list of errors if failed."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Verify email successfully",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "[\"Xác nhận email thành công. Bạn có thể đăng nhập\"]")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid token",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                value = "{\n" +
                                    "  \"error\": \"Invalid Token Error\",\n" +
                                    "  \"message\": \"Token không hợp lệ. Vui lòng yêu cầu đăng ký mới\",\n" +
                                    "  \"path\": \"/api/auth/verify-email\",\n" +
                                    "  \"status\": 400\n" +
                                    "}"
                            )                   
                     )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already verified",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                value = "{\n" +
                                    "  \"error\": \"User Already Verified Error\",\n" +
                                    "  \"message\": \"Người dùng đã xác thực\",\n" +
                                    "  \"path\": \"/api/auth/verify-email\",\n" +
                                    "  \"status\": 409\n" +
                                    "}"
                            )
                    )
            )
    })

    @GetMapping("/api/auth/verify-email")
    public ResponseEntity<?> createUser(@Schema(description = "Email verification token", example = "1234567890") @RequestParam String token) {
        userService.verifyEmail(token);
        return ResponseEntity.ok("Xác nhận email thành công. Bạn có thể đăng nhập");
    }

    /**
     * This endpoint resend verification email
     * @param email user email
     * @return ResponseEntity returns the notification if email verification is sent successfully. Otherwise, it returns errors
     */ 
    @Operation(
        summary = "Resend verification email",
        description = "Resend verification email to the user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resend verification email successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Email xác nhận được gửi. Vui lòng kiểm tra inbox.\"]")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Invalid token\",\n" +
                        "  \"message\": \"Token không hợp lệ. Vui lòng yêu cầu đăng ký mới\",\n" +
                        "  \"path\": \"/api/auth/resend-verification\",\n" +
                        "  \"status\": 400\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/api/auth/resend-verification\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )
            )
        ),  
        @ApiResponse(
            responseCode = "429",
            description = "Resend limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resend Limit Exceeded Error\",\n" +
                        "  \"message\": \"Bạn đã đạt giới hạn gửi lại. Vui lòng thử lại sau.\",\n" +
                        "  \"path\": \"/api/auth/resend-verification\",\n" +
                        "  \"status\": 429\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "User already verified",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"User Already Verified Error\",\n" +
                        "  \"message\": \"Người dùng đã xác thực\",\n" +
                        "  \"path\": \"/api/auth/resend-verification\",\n" +
                        "  \"status\": 409\n" +
                        "}"
                )
            )
        )
    })
    @PostMapping("/api/auth/resend-verification")
    public ResponseEntity<?> resendVerification(@Schema(description = "Email of the user", example = "test@example.com") @RequestParam String email) {

        userService.resendVerificationEmail(email);
        return ResponseEntity.ok("Email xác nhận được gửi. Vui lòng kiểm tra inbox.");

    }

    /**
     * This endpoint handles the login process
     * @param userLoginDTO This DTO contains login information
     * @param response This is used to set cookie
     * @return ResponseEntity returns access token if the user logins successfully. Otherwise, it returns list of errors
     */ 
    @Operation(
        summary = "Login",
        description = "Login to the system"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{ \"accessToken\": \"1234567890\" }"
                )   
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Bad Credentials Error\",\n" +
                        "  \"message\": \"Username hoặc mật khẩu không chính xác\",\n" +
                        "  \"path\": \"/api/auth/login\",\n" +
                        "  \"status\": 401\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "User not verified", 
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"User Not Verified Error\",\n" +
                        "  \"message\": \"Người dùng chưa xác thực. Vui lòng kiểm tra email để xác thực tài khoản.\",\n" +
                        "  \"path\": \"/api/auth/login\",\n" +
                        "  \"status\": 403\n" +
                        "}"
                )
            )
        )
            
    })
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginDTO userLoginDTO, HttpServletResponse response){
        try {
            TokenResponseDTO tokens = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());
            System.out.println(tokens);
            /*Bo sung: */
            Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true); //chỉ hoạt động với HTTPS
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); //7 ngày
            refreshCookie.setAttribute("SameSite", "None"); // Cho phép gửi cookie cross-origin
            response.addCookie(refreshCookie);
            return ResponseEntity.ok(Collections.singletonMap("accessToken", tokens.getAccessToken()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    /**
     * This endpoint refreshes new access token
     * @param refreshToken get refreshToken from cookie
     * @return ResponseEntity returns the new access token if new access token is created successfully. Otherwise, it returns list of errors
     */
    @Operation(
        summary = "Refresh access token",
        description = "Refresh access token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Refresh access token successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{ \"accessToken\": \"1234567890\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Invalid Token Error\",\n" +
                        "  \"message\": \"Token không hợp lệ\",\n" +
                        "  \"path\": \"/api/auth/refresh\",\n" +
                        "  \"status\": 400\n" +
                        "}"
                )            
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/api/auth/refresh\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )            
            )
        )
    })
    @GetMapping("/api/auth/refresh")
    public ResponseEntity<?> refresh(@Schema(description = "Refresh token", example = "1234567890") @CookieValue("refreshToken") String refreshToken){
        String newAccessToken = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Collections.singletonMap("accessToken", newAccessToken));
    }

    /**
     * This endpoint gets information of all users
     * @return ResponseEntity returns a list of UserDTO
     */
    @Operation(
        summary = "Get all users",
        description = "Get all users"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get all users successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)),
                examples = @ExampleObject(
                    value = "[{\"id\": 1,\"fullName\": \"John Doe\", \"username\": \"user1\", \"phone\": \"1234567890\", \"email\": \"user1@example.com\", \"role\": {\"id\": 1, \"name\": \"ADMIN\"}, \"enabled\": true}, {\"id\": 2, \"fullName\": \"Jane Doe\", \"username\": \"user2\", \"phone\": \"1234567890\", \"email\": \"user2@example.com\", \"role\": {\"id\": 2, \"name\": \"USER\"}, \"enabled\": true}]"
                )
            )
        )
    })  
    @GetMapping("/get-all-users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * This endpoint gets information of a specific user based on user id
     * @param id user id
     * @return ResponseEntity returns the information of user if it works successfully. Otherwise, it returns list of errors
     */
    @Operation(
        summary = "Get user by id",
        description = "Get user by id"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get user by id successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = "{\"id\": 1, \"fullName\": \"John Doe\", \"username\": \"user1\", \"phone\": \"1234567890\", \"email\": \"user1@example.com\", \"role\": {\"id\": 1, \"name\": \"ADMIN\"}, \"enabled\": true}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/user/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )            
            )
        )
    })
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@Schema(description = "User id", example = "1") @PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);

    }

    /**
     * This endpoint gets information of current user
     * @param user Current user from SecurityContext
     * @return ResponseEntity returns the information of current user
     */
    @Operation(
        summary = "Get current user",
        description = "Get current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Get current user successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = "{\"id\": 1, \"fullName\": \"John Doe\", \"username\": \"user1\", \"phone\": \"1234567890\", \"email\": \"user1@example.com\", \"role\": {\"id\": 1, \"name\": \"ADMIN\"}, \"enabled\": true}"
                )
            )
        )
    })
    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(user);
    }

    /**
     * This endpoint updates user's password
     * @param user Current user from SecurityContext
     * @param userChangePwdDTO DTO contains information about changed password
     * @param result Validation result
     * @return ResponseEntity returns the updated user if password is changed successfully. Otherwise, it return errors
     */
    @Operation(
        summary = "Update user's password",
        description = "Update user's password"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Update user's password successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = "{\"id\": 1, \"fullName\": \"John Doe\", \"username\": \"user1\", \"phone\": \"1234567890\", \"email\": \"user1@example.com\", \"role\": {\"id\": 1, \"name\": \"ADMIN\"}, \"enabled\": true}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid password",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Mật khẩu sai\", \"Mật khẩu không khớp\"]")
            )
        )
    })
    @PutMapping("/user/me/change-password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal User user, @RequestBody @Valid UserChangePwdDTO userChangePwdDTO, BindingResult result){
        List<String> errorMessages = new ArrayList<>(result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList());
        if(!passwordEncoder.matches(userChangePwdDTO.getOldPassword(), user.getPassword())){
            errorMessages.add("Mật khẩu sai");
        }
        if(!userChangePwdDTO.getNewPassword().equals(userChangePwdDTO.getRetypePassword())){
            errorMessages.add("Mật khẩu không khớp");
        }
        if(!errorMessages.isEmpty()){
            return ResponseEntity.badRequest().body(errorMessages);
        }
        User updatedUser = userService.updatePassword(user, userChangePwdDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * This endpoint sends link to email
     * @param userForgotPwdDTO DTO contains email to send reset password link
     * @param result Validation result
     * @return ResponseEntity returns the information of current user
     */
    @Operation(
        summary = "Send reset password link to email",
        description = "Send reset password link to email"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Send reset password link to email successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Link đặt lại mật khẩu đã được gửi thành công!\"]")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/api/auth/forgot-password\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )            
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid email",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "[\n" +
                        "  \"Email là bắt buộc\",\n" +
                        "]"
                )            
            )
        )
    })  
    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid UserForgotPwdDTO userForgotPwdDTO, BindingResult result){
        List<String> errorMessages = new ArrayList<>(result.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList());
        if(!errorMessages.isEmpty()){
            return ResponseEntity.badRequest().body(errorMessages);
        }
        userService.sendPasswordResetEmail(userForgotPwdDTO.getEmail());
        return ResponseEntity.ok("Link đặt lại mật khẩu đã được gửi thành công!");

    }

    /**
     * Reset user password
     * 
     * @param token Password reset token
     * @param userResetPwdDTO DTO contains new password
     * @param result Validation result
     * @return ResponseEntity containing result message
     */
    @Operation(
        summary = "Reset user password",
        description = "Reset user password"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",   
            description = "Reset user password successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Thay đổi mật khẩu thành công\"]")
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Invalid Token Error\",\n" +
                        "  \"message\": \"Token không hợp lệ. Vui lòng yêu cầu đăng ký mới\",\n" +
                        "  \"path\": \"/api/auth/verify-email\",\n" +
                        "  \"status\": 400\n" +
                        "}"
                )               
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/api/auth/reset-password\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )            )
        )
    })
    @PutMapping("/api/auth/reset-password")
    public ResponseEntity<?> resetPassword(@Schema(description = "Password reset token", example = "1234567890") @RequestParam String token, @RequestBody @Valid UserResetPwdDTO userResetPwdDTO, BindingResult result){
        List<String> errorMessages = new ArrayList<>(result.getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList());
        if(!errorMessages.isEmpty()){
            return ResponseEntity.badRequest().body(errorMessages);
        }
        userService.resetPassword(token, userResetPwdDTO);
        return ResponseEntity.ok("Thay đổi mật khẩu thành công");
    }

    /**
     * User logout
     * 
     * @param user Current user
     * @param response HttpServletResponse for clearing cookies
     * @return ResponseEntity containing result message
     */
    @Operation(
        summary = "Logout",
        description = "Logout"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User logout successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Đăng xuất thành công\"]")
            )
        )
    })
    @GetMapping("/api/auth/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal User user, HttpServletResponse response){
            userService.logout(user);
            response.addHeader(HttpHeaders.SET_COOKIE, "refreshToken=; HttpOnly; Path=/; Max-Age=0; Secure; SameSite=Strict");
            return ResponseEntity.ok("Đăng xuất thành công");
    }

    /**
     * Delete user by ID
     * 
     * @param id User ID to delete
     * @return ResponseEntity containing result message
     */
    @Operation(
        summary = "Delete user by ID",
        description = "Delete user by ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Delete user by ID successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Đã xóa người dùng thành công\"]")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/api/user/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )            
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Admin deletion not allowed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Admin Deletion Not Allowed Error\",\n" +
                        "  \"message\": \"Không thể xóa tài khoản admin\",\n" +
                        "  \"path\": \"/api/user/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )               
            )
        )
    })
    @DeleteMapping("/api/user/{id}")
    public ResponseEntity<String> deleteUserById(@Schema(description = "User id", example = "1") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Đã xóa người dùng thành công");
    }

    /**
     * Update user information
     * 
     * @param userUpdateDTO DTO containing user update information
     * @param id User ID to update
     * @param result Validation result
     * @return ResponseEntity containing updated user information
     */
    @Operation(
        summary = "Update user information",
        description = "Update user information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Update user information successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                    value = "{\"id\": 1, \"fullName\": \"John Doe\", \"username\": \"user1\", \"phone\": \"1234567890\", \"email\": \"user1@example.com\", \"role\": {\"id\": 1, \"name\": \"ADMIN\"}, \"enabled\": true}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid data",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Tên không được để trống\", \"Số điện thoại không được để trống\"]")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found Error\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/api/user/1\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )            
            )
        )
    })
    @PutMapping("/api/user/{id}")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO userUpdateDTO, @Schema(description = "User id", example = "1") @PathVariable Long id, BindingResult result){
        List<String> errorMessages = new ArrayList<>(result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList());
        if(!errorMessages.isEmpty()){
            return ResponseEntity.badRequest().body(errorMessages);
        }
        User updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user role
     * 
     * @param role New role
     * @param id User ID to update
     * @return ResponseEntity containing result message
     */
    @Operation(
        summary = "Update user role",
        description = "Update user role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Update user role successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "[\"Cập nhật vai trò user thành công\"]")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"error\": \"Resource Not Found\",\n" +
                        "  \"message\": \"Người dùng không tồn tại\",\n" +
                        "  \"path\": \"/api/user/1/role\",\n" +
                        "  \"status\": 404\n" +
                        "}"
                )            
            )
        )
    })
    @PutMapping("/api/user/{id}/role")
    public ResponseEntity<?> updateUserRole(@Schema(description = "Role of user", example = "OWNER") @RequestParam Role role, @Schema(description = "User id", example = "1") @PathVariable Long id){
            userService.updateUserRole(id, role);
            return ResponseEntity.ok("Cập nhật vai trò user thành công");

    }


}
