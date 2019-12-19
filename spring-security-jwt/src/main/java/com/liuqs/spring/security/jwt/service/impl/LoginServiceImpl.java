package com.liuqs.spring.security.jwt.service.impl;

import com.liuqs.spring.security.jwt.service.LoginService;
import com.liuqs.spring.security.jwt.service.UserService;
import com.liuqs.spring.security.jwt.utils.JwtTokenUtil;
import com.liuqs.spring.security.jwt.vo.LoginReqVo;
import com.liuqs.spring.security.jwt.vo.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public Token sms(LoginReqVo reqVo) {
        // 1. 从 redis 或者 数据库中获取验证码
        String mobile = reqVo.getMobile();
        String storageCode = findByMobile(mobile);
        if (!storageCode.equals(reqVo.getCode())) {
            throw new BadCredentialsException("验证码不正确!");
        }
        // 2. 获取用户信息
        UserDetails userDetails = userService.findByMobile(mobile);

        // 3. 获取token
        return jwtTokenUtil.generateToken(userDetails);
    }

    @Override
    public Token passport(LoginReqVo reqVo) {
        // 1. 获取用户信息
        UserDetails userDetails = userService.findByUsername(reqVo.getUsername());
        if (!userDetails.getPassword().equals(reqVo.getPassword())) {
            throw new BadCredentialsException("密码不正确!");
        }

        // 2. 获取token
        return jwtTokenUtil.generateToken(userDetails);
    }

    /**
     * 通过手机号查询验证码
     * <p>
     * 假设从 redis 或者 数据库 中获取到验证码 1234
     *
     * @param mobile
     * @return
     */
    private String findByMobile(String mobile) {
        return "1234";
    }
}
