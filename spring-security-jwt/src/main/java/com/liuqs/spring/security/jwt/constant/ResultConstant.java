package com.liuqs.spring.security.jwt.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ResultConstant {
    /**
     *
     */
    SUCCESS(200, "成功"),
    UNAUTHORIZED(1001, "未认证"),
	PERMISSION_DENIED(1002,"没有权限")
            ;

    private int code;
    private String desc;
}
