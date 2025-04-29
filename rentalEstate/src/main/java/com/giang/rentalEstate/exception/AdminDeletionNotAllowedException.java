package com.giang.rentalEstate.exception;

public class AdminDeletionNotAllowedException extends RuntimeException{
    public AdminDeletionNotAllowedException(String message){
        super(message);
    }
}
