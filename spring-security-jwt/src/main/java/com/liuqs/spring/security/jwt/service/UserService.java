package com.liuqs.spring.security.jwt.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    UserDetails findByUsername(String username);

    UserDetails findByMobile(String mobile);

}
