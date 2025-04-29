package com.giang.rentalEstate.exception;

public class PropertyAlreadySavedException extends RuntimeException {
    public PropertyAlreadySavedException(String message) {
        super(message);
    }
} 