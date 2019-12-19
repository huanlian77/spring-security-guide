package com.liuqs.spring.security.jwt.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Token {
    private String token;
    private String tokenHead;
}
