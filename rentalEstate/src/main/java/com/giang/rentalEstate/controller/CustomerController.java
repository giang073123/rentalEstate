package com.giang.rentalEstate.controller;

import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * This controller manages customer-related operations.
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Controller", description = "APIs for customer management")
public class CustomerController {
    private final CustomerService customerService;

    /**
     * Retrieves all customers associated with a specific owner based on their id
     *
     * @param ownerId the id of the owner whose customers are to be retrieved
     * @return a list of customers associated with the specified owner
     */
    @Operation(
        summary = "Get customers by owner ID",
        description = "Retrieves all customers associated with a specific owner"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved customers",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class),
                examples = @ExampleObject(
                value = "[\n" +
                        "  {\n" +
                        "    \"id\": 2,\n" +
                        "    \"fullName\": \"Nguyen Van A\",\n" +
                        "    \"username\": \"nguyenvana\",\n" +
                        "    \"phone\": \"0123456789\",\n" +
                        "    \"email\": \"a@gmail.com\",\n" +
                        "    \"role\": { \"id\": 3, \"name\": \"CUSTOMER\" }\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": 3,\n" +
                        "    \"fullName\": \"Tran Thi B\",\n" +
                        "    \"username\": \"tranthib\",\n" +
                        "    \"phone\": \"0987654321\",\n" +
                        "    \"email\": \"b@gmail.com\",\n" +
                        "    \"role\": { \"id\": 3, \"name\": \"CUSTOMER\" }\n" +
                        "  }\n" +
                        "]"
            )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Owner not found",
            content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = "{ \"error\": \"Resource Not Found Error\", \"message\": \"Không tìm thấy chủ sở hữu\", \"path\": \"/api/auth/customers/owner/1\", \"status\": 404 }"
                )
            )
        )
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<User>> getCustomersByOwnerId(@Schema(description = "User id", example = "1") @PathVariable Long ownerId) {
        return ResponseEntity.ok(customerService.getCustomersByOwnerId(ownerId));
    }


} 