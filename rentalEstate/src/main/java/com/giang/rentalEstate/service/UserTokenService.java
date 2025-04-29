package com.giang.rentalEstate.service;

import com.giang.rentalEstate.model.User;

public interface UserTokenService {
    void createAndSendVerificationToken(String emailUser, User user);
}
