package com.liuqs.spring.security.jwt.controller;

import com.liuqs.spring.security.jwt.service.LoginService;
import com.liuqs.spring.security.jwt.vo.LoginReqVo;
import com.liuqs.spring.security.jwt.vo.Result;
import com.liuqs.spring.security.jwt.vo.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/sms")
    public Result<Token> sms(@RequestBody LoginReqVo reqVo) {
        return Result.success(loginService.sms(reqVo));
    }

    @PostMapping("/passport")
    public Result<Token> passport(@RequestBody LoginReqVo reqVo) {
        return Result.success(loginService.passport(reqVo));
    }
}
