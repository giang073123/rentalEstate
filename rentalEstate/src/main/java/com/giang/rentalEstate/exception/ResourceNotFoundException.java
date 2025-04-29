package com.giang.rentalEstate.exception;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String messsage){
        super(messsage);
    }
}
