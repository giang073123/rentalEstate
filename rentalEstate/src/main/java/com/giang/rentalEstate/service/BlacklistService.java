package com.giang.rentalEstate.service;

public interface BlacklistService {
    void addToBlacklist(String token);
    boolean isBlacklisted(String token);
}
