package com.liuqs.spring.security.jwt.vo;

import lombok.Data;

@Data
public class LoginReqVo {

    private String username;
    private String password;
    private String mobile;
    private String code;
}
