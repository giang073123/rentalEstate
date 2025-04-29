package com.giang.rentalEstate.exception;

public class RentalRequestNotAuthorizedException extends RuntimeException {
    public RentalRequestNotAuthorizedException(String message) {
        super(message);
    }
} 