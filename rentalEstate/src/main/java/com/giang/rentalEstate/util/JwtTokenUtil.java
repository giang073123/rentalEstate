package com.giang.rentalEstate.util;

import com.giang.rentalEstate.model.User;
import com.giang.rentalEstate.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.jwtSecret}")
    private String jwtSecret;
    @Value("${jwt.accesstoken.expiration}")
    private int accessTokenExpiration;
    @Value("${jwt.refreshtoken.expiration}")
    private int refreshTokenExpiration;

    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");
        logger.debug("Authorization Header: {}", bearerToken);
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
    public String generateTokenFromUsername(User user) {
        String role = user.getRole().getName().toString();
        System.out.println("O day" + role);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("tokenVersion", user.getTokenVersion())
                .claim("role", role)
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + accessTokenExpiration))
                .signWith(key())
                .compact();
    }
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("tokenVersion", user.getTokenVersion())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + refreshTokenExpiration))
                .signWith(key())
                .compact();
    }


    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public Claims extractClaims(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload();
    }
    public String getUserNameFromJwtToken(String token) {
        return extractClaims(token).getSubject();
    }
    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }
    public long getExpiryTime(String token){
        return extractClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    public boolean validateToken(String token, User user) {
        int tokenVersion = extractClaims(token)
                .get("tokenVersion", Integer.class);
        return user.getUsername().equals(getUserNameFromJwtToken(token)) && !isTokenExpired(token) && tokenVersion == user.getTokenVersion();
    }

}
