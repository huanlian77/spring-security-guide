package com.liuqs.spring.security.order.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class OrderController {

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