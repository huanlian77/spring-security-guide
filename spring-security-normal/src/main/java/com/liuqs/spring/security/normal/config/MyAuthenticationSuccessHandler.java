package com.liuqs.spring.security.normal.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class MyAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        doLoginSuccess(authentication);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     * 登录成功操作
     *
     * @param authentication
     */
    private void doLoginSuccess(Authentication authentication) {
        // TODO: 2019/12/17 登录日志记录
        String username = (String) authentication.getPrincipal();
        System.out.println(username + "登录了!");
    }


}
