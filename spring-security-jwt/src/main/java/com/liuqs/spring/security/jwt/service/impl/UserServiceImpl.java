package com.liuqs.spring.security.jwt.service.impl;

import com.liuqs.spring.security.jwt.security.UserDetailsX;
import com.liuqs.spring.security.jwt.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private static final List<UserDetailsX> userDetailsXList = new ArrayList<>();

    static {
        userDetailsXList.add(new UserDetailsX("zhangsan", "123456", "17600000001"));
        userDetailsXList.add(new UserDetailsX("lisi", "123456", "17600000002"));
    }

    public UserDetails findByUsername(String username) {
        Optional<UserDetailsX> userDetailsXOptional = userDetailsXList.stream().filter(elem -> username.equals(elem.getUsername())).findFirst();
        return userDetailsXOptional.orElse(null);
    }

    public UserDetails findByMobile(String mobile) {
        Optional<UserDetailsX> userDetailsXOptional = userDetailsXList.stream().filter(userDetailsX -> mobile.equals(userDetailsX.getMobile())).findFirst();
        return userDetailsXOptional.orElse(null);
    }
}
