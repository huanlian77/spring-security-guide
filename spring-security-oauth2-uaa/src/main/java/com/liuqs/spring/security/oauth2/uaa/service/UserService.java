package com.liuqs.spring.security.oauth2.uaa.service;

import com.liuqs.spring.security.oauth2.uaa.security.UserDetailsX;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final List<UserDetailsX> userDetailsXList = new ArrayList<>();

    static {
        userDetailsXList.add(new UserDetailsX("zhangsan", "123456", "17600000001"));
        userDetailsXList.add(new UserDetailsX("lisi", "123456", "17600000002"));
    }

    public UserDetails findByUsername(String username) {
        Optional<UserDetailsX> userDetailsXOptional = userDetailsXList.stream().filter(elem -> username.equals(elem.getUsername())).findFirst();
        return userDetailsXOptional.orElse(null);
    }


}
