package com.giang.rentalEstate.exception;

public class ResendLimitExceededException extends RuntimeException{
    public ResendLimitExceededException(String message) {
        super(message);
    }

}
