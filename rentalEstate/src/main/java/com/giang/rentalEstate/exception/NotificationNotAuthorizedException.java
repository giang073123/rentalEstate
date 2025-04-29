package com.giang.rentalEstate.exception;

public class NotificationNotAuthorizedException extends RuntimeException{
    public NotificationNotAuthorizedException(String message){
        super(message);
    }
}
