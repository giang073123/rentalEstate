package com.giang.rentalEstate.controller;

import com.giang.rentalEstate.model.Notification;
import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;
/**
 * Controller handling notification-related operations including retrieval and management of user notifications
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "APIs for notification management")
public class NotificationController {
    private final NotificationService notificationService;

    /**
     * Get all notifications of current user
     * 
     * @param user Current user
     * @return ResponseEntity containing list of notifications
     */
    @Operation(
        summary = "Get user notifications",
        description = "Get all notifications of the current user, ordered by creation time (newest first)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notifications retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Notification.class),
                examples = @ExampleObject(
                    value = "[{\"id\":1,\"title\":\"Yêu cầu thuê nhà mới\",\"content\":\"Bạn có yêu cầu thuê nhà mới từ Nguyễn Văn A\",\"isRead\":false,\"user\":{\"id\":1,\"fullName\":\"Nguyen Van A\",\"email\":\"nguyenvana@example.com\"},\"property\":{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\"},\"rentalRequest\":{\"id\":1,\"status\":\"PENDING\"},\"createdAt\":\"2024-03-20 10:30:00\"},{\"id\":2,\"title\":\"Bất động sản đã được duyệt\",\"content\":\"Bất động sản của bạn đã được duyệt bởi admin\",\"isRead\":true,\"user\":{\"id\":1,\"fullName\":\"Nguyen Van A\",\"email\":\"nguyenvana@example.com\"},\"property\":{\"id\":2,\"title\":\"Nhà trọ cho thuê\"},\"rentalRequest\":null,\"createdAt\":\"2024-03-19 15:45:00\"}]"
                )
            )
        )
    })
    @GetMapping("/api/notifications")
    public ResponseEntity<List<Notification>> getUserNotifications(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user));
    }
    /**
     * Get all unread notifications for a user
     * 
     * @param user Current user
     * @return ResponseEntity containing list of unread notifications
     */
    @Operation(
        summary = "Get unread notifications",
        description = "Get all unread notifications of the current user, ordered by creation time (newest first)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Unread notifications retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Notification.class),
                examples = @ExampleObject(
                    value = "[{\"id\":1,\"title\":\"Yêu cầu thuê nhà mới\",\"content\":\"Bạn có yêu cầu thuê nhà mới từ Nguyễn Văn A\",\"isRead\":false,\"user\":{\"id\":1,\"fullName\":\"Nguyen Van A\",\"email\":\"nguyenvana@example.com\"},\"property\":{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\"},\"rentalRequest\":{\"id\":1,\"status\":\"PENDING\"},\"createdAt\":\"2024-03-20 10:30:00\"},{\"id\":3,\"title\":\"Bất động sản bị từ chối\",\"content\":\"Bất động sản của bạn đã bị từ chối bởi admin\",\"isRead\":false,\"user\":{\"id\":1,\"fullName\":\"Nguyen Van A\",\"email\":\"nguyenvana@example.com\"},\"property\":{\"id\":3,\"title\":\"Căn hộ cao cấp\"},\"rentalRequest\":null,\"createdAt\":\"2024-03-18 09:15:00\"}]"
                )
            )
        )
    })
    @GetMapping("/api/notifications/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user));
    }
    /**
     * Get the count of unread notifications for a user
     * 
     * @param user Current user
     * @return ResponseEntity containing the count of unread notifications
     */
    @Operation(
        summary = "Get unread notification count",
        description = "Get the total number of unread notifications for the current user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Unread notification count retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(type = "integer"),
                examples = @ExampleObject(
                    value = "5"
                )
            )
        )
    })
    @GetMapping("/api/notifications/unread/count")
    public ResponseEntity<Long> getUnreadNotificationCount(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationCount(user));
    }
    /**
     * Mark a notification as read based on notification id
     * 
     * @param notificationId Notification id
     * @param user Current user
     * @return ResponseEntity containing the result message
     */ 
    @Operation(
        summary = "Mark notification as read",
        description = "Mark a specific notification as read. Only the owner of the notification can mark it as read"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Notification marked as read successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Notification.class),
                examples = @ExampleObject(
                    value = "{\"id\":1,\"title\":\"Yêu cầu thuê nhà mới\",\"content\":\"Bạn có yêu cầu thuê nhà mới từ Nguyễn Văn A\",\"isRead\":true,\"user\":{\"id\":1,\"fullName\":\"Nguyen Van A\",\"email\":\"nguyenvana@example.com\"},\"property\":{\"id\":1,\"title\":\"Căn hộ cho thuê tại Quận 1\"},\"rentalRequest\":{\"id\":1,\"status\":\"PENDING\"},\"createdAt\":\"2024-03-20 10:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Notification not found",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\":\"Resource Not Found Error\",\"message\":\"Thông báo không tồn tại\",\"path\":\"/api/notifications/1/read\",\"status\":404}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = "{\"error\":\"Access Denied Error\",\"message\":\"Bạn không có quyền đánh dấu đã đọc cho thông báo này\",\"path\":\"/api/notifications/1/read\",\"status\":403}"
                )
            )
        )
    })
    @PutMapping("/api/notifications/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @Schema(
                description = "Notification ID",
                example = "1"
            ) @PathVariable Long notificationId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, user));
    }
} 