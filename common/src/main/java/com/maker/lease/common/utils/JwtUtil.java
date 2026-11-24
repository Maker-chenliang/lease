package com.maker.lease.common.utils;

import com.maker.lease.common.exception.LeaseException;
import com.maker.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.interfaces.PBEKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    public static SecretKey secretKey = Keys.hmacShaKeyFor("lease-maker-key-3pRJ667WbSzcxnZU".getBytes(StandardCharsets.UTF_8));

    public static String generateJwt(Long userId,String userName,String userIdentity){
        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000*24*10))
                .setSubject("LOGIN_USER")
                .claim("userId", userId)
                .claim("userName", userName)
                .claim("userIdentity", userIdentity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static Claims parseJwt(String jwt){
        try{
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(jwt);
            Claims body = claimsJws.getBody();
            return body;
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }
    //获取一个长期的jwt
    public static void main(String args[])
    {
        String s = generateJwt(1L, "YOUR-PHONENUMBER", "APP_USER");
        System.out.println(s);
    }
}
