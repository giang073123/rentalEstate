package com.giang.rentalEstate.service;

public interface EmailService {
    void sendEmail(String to, String subject, String text);
}
