package com.emosation.emosation.Util;


import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access.expiration}")
    private long ACCESS_TOKEN_VALIDITY;

    @Value("${jwt.refresh.expiration}")
    private long REFRESH_TOKEN_VALIDITY;


    public String generateToken(String id) {
        byte [] decodedKey = Base64.getDecoder().decode(SECRET_KEY);

        return Jwts.builder().setClaims(claims(id)).setSubject(id)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS256, decodedKey)
                .compact();

    }

    public Map<String, Object> claims(String id) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("Id",id);  // 파이썬으로 따지자면 dict타입인데 {"id" : id } 이런 식 인듯.
        return claims;

    }

    public Claims getClaim(String token){  // claim에서 expiration date를 뽑아오기위함..
        try{
            byte [] decodedKey = Base64.getDecoder().decode(SECRET_KEY);

            return Jwts.parser().setSigningKey(decodedKey).parseClaimsJws(token).getBody();

        }catch (ExpiredJwtException e){
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), e.getMessage());
        }catch (SignatureException e){
            throw new JwtException("사인키 부적합",e);
        }catch (IllegalArgumentException e){
            throw new JwtException("토큰이 없습니다",e);
        }

    }

    public String extId(String token){
        return getClaim(token).getSubject();
    }


    public boolean isExp(String token){
        try{
            Claims claims = getClaim(token);
            Date Exp = claims.getExpiration();
            return Exp.before(new Date());
        } catch (JwtException e){
            System.out.println("jwt is Expired" + e.getMessage());
            return true;
        }
    }

    public String generateRefToken(String id){
        byte[] decodedKey = Base64.getDecoder().decode(SECRET_KEY);
        return Jwts.builder()
                .setClaims(claims(id)).setSubject(id).
                setIssuedAt(new Date()).
                setExpiration(new Date(System.currentTimeMillis()+REFRESH_TOKEN_VALIDITY * 1000 )).
                signWith(SignatureAlgorithm.HS256, decodedKey).
                compact();

    }








}
