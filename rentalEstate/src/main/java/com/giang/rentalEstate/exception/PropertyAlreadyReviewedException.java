package com.giang.rentalEstate.exception;

public class PropertyAlreadyReviewedException extends RuntimeException {
    public PropertyAlreadyReviewedException(String message) {
        super(message);
    }
}
