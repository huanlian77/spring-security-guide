package com.liuqs.spring.security.normal.security.passport;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PassportAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {


    private static final String FILTER_URL = "/login/passport";
    private static final String USERNAME_PARAMETER = "username";
    private static final String PASSWORD_PARAMETER = "password";
    private boolean postOnly = true;


    public PassportAuthenticationProcessingFilter() {
        // 对 '/login/passport' POST 请求 URL 进行拦截
        super(new AntPathRequestMatcher(FILTER_URL, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }

        // 用户名称
        String username = obtainUsername(request);
        if (StringUtils.isEmpty(username)) {
            throw new AuthenticationServiceException("登录账号不能为空");
        }
        // 密码
        String password = obtainPassword(request);
        if (StringUtils.isEmpty(password)) {
            throw new AuthenticationServiceException("密码不能为空");
        }
        PassportAuthenticationToken authRequest = new PassportAuthenticationToken(username, password);
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(PASSWORD_PARAMETER);
    }

    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(USERNAME_PARAMETER);
    }
}
