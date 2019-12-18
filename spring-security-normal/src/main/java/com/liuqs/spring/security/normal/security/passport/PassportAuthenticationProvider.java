package com.liuqs.spring.security.normal.security.passport;

import com.liuqs.spring.security.normal.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class PassportAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PassportAuthenticationToken token = (PassportAuthenticationToken) authentication;

        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(token.getUsername());
        if (userDetails == null) {
            throw new UsernameNotFoundException("用户名不存在!");
        }

        if (!userDetails.getPassword().equals(token.getPassword())) {
            throw new BadCredentialsException("用户名密码不匹配");
        }
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (PassportAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
