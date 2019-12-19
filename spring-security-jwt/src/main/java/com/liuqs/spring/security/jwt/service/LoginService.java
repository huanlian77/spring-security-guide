package com.liuqs.spring.security.jwt.service;

import com.liuqs.spring.security.jwt.vo.LoginReqVo;
import com.liuqs.spring.security.jwt.vo.Token;

public interface LoginService {
    
    Token sms(LoginReqVo reqVo);

    Token passport(LoginReqVo reqVo);
}
