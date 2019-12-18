package com.liuqs.spring.security.normal.security.passport;

import org.springframework.security.authentication.AbstractAuthenticationToken;


public class PassportAuthenticationToken extends AbstractAuthenticationToken {

    private String username;
    private String password;

    public PassportAuthenticationToken(String username, String password) {
        super(null);
        this.username = username;
        this.password = password;
    }


    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
