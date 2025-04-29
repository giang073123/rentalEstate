package com.giang.rentalEstate.service.impl;

import com.giang.rentalEstate.service.BlacklistService;
import com.giang.rentalEstate.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {
    private final Map<String, Long> blackList = new ConcurrentHashMap<>();
    private final JwtTokenUtil jwtTokenUtil;
    @Override
    public void addToBlacklist(String token) {
        String jwt = token.replace("Bearer ", "");
        long expiryTime = jwtTokenUtil.getExpiryTime(jwt);
        blackList.put(jwt, System.currentTimeMillis() + expiryTime);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String jwt = token.replace("Bearer ", "");
        return blackList.containsKey(token) && blackList.get(jwt) > System.currentTimeMillis();
    }
    @Scheduled(fixedRate = 60000) // Chạy mỗi 60 giây
    public void cleanUp() {
        long now = System.currentTimeMillis();
        blackList.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
