package com.giang.rentalEstate.exception;

public class UserAlreadyVerifiedException extends RuntimeException{
    public UserAlreadyVerifiedException(String message) {
        super(message);
    }
}
