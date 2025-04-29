package com.giang.rentalEstate.exception;

public class DuplicateRentalRequestException extends RuntimeException{
    public DuplicateRentalRequestException(String message){
        super(message);
    }
}
