package com.giang.rentalEstate.service.impl;

import com.giang.rentalEstate.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    @Override
    public void sendEmail(String to, String subject, String text) {
        System.out.println("Gửi email cho email: " + to);
        System.out.println("Gửi email cho subject: " + subject);
        System.out.println("Gửi email cho text: " + text);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        javaMailSender.send(mailMessage);
        System.out.println("Gửi email thành công");
    }
}
