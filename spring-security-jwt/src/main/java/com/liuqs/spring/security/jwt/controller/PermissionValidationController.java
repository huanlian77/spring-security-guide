package com.liuqs.spring.security.jwt.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限验证
 */
@RestController
@RequestMapping
public class PermissionValidationController {

    @RequestMapping("/sayHi")
    @PreAuthorize("hasAuthority('pmv:say:hi')")
    public String sayHi() {
        return "sayHi";
    }

    @RequestMapping("/sayHello")
    @PreAuthorize("hasAuthority('pmv:say:hello')")
    public String sayHello(){
        return "sayHello";
    }
}
