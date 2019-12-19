package com.liuqs.spring.security.jwt.utils;

import com.liuqs.spring.security.jwt.vo.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

public class JwtTokenUtil {

    private static String CLAIM_KEY_USERNAME = "subject";
    private static String CLAIM_KEY_CREATED = "created";
    private static String CLAIM_KEY_AUTHORITY = "authority";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成token
     *
     * @param userDetails
     * @return
     */
    public Token generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_AUTHORITY, authorities2Str(userDetails.getAuthorities()));

        String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        return new Token().setToken(token).setTokenHead(tokenHead);
    }


    private String authorities2Str(Collection<? extends GrantedAuthority> authorities) {
        List<String> authorities2StrList = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return String.join(",", authorities2StrList);
    }

    /**
     * 生成token的过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 根据token获取用户名
     *
     * @param token
     * @return
     */
    public String getUserNameByToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (String) claims.get(CLAIM_KEY_USERNAME);
    }

    /**
     * 从token中获取JWT中的负载
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 根据token获取权限
     *
     * @param token
     * @return
     */
    public List<SimpleGrantedAuthority> getGrantedAuthorityByToken(String token) {
        Claims claims = getClaimsFromToken(token);
        String authorities = (String) claims.get(CLAIM_KEY_AUTHORITY);
        return Arrays.stream(authorities.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}