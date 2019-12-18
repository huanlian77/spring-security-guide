package com.liuqs.spring.security.normal.security.sms;

import com.liuqs.spring.security.normal.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public class SmsAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken token = (SmsAuthenticationToken) authentication;

        String mobile = token.getMobile();
        String code = token.getCode();
        // 1.通过手机号从 session 或者 redis 中查找 code
        String storageCode = findCodeByMobile(mobile);
        // 2.判断验证码是否过期、正确
        if (!code.equals(storageCode)) {
            throw new BadCredentialsException("验证码不正确!");
        }
        UserDetails userDetails = userDetailsServiceImpl.loadUserByMobile(token.getMobile());
        if (userDetails == null) {
            // 这里通常逻辑是做成手机号不存在自动注册
            throw new UsernameNotFoundException("手机号不存在!");
        }
        UsernamePasswordAuthenticationToken result = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(), userDetails.getPassword(), userDetails.getAuthorities());
        result.setDetails(authentication.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (SmsAuthenticationToken.class.isAssignableFrom(authentication));
    }


    /**
     * 这里假设从  session 或者 redis 中获取
     *
     * @param mobile
     * @return
     */
    private String findCodeByMobile(String mobile) {
        return "1234";
    }
}
