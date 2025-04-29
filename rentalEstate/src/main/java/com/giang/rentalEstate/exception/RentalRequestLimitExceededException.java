package com.giang.rentalEstate.exception;

public class RentalRequestLimitExceededException extends RuntimeException{
    public RentalRequestLimitExceededException(String message){
        super(message);
    }
}
