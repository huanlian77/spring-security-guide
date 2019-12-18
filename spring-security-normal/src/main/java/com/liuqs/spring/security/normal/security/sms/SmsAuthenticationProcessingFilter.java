package com.liuqs.spring.security.normal.security.sms;

import com.liuqs.spring.security.normal.security.passport.PassportAuthenticationToken;
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

public class SmsAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {


    private static final String FILTER_URL = "/login/sms";
    private static final String MOBILE_PARAMETER = "mobile";
    private static final String CODE_PARAMETER = "code";
    private boolean postOnly = true;


    public SmsAuthenticationProcessingFilter() {
        // 对 '/login/sms' POST 请求 URL 进行拦截
        super(new AntPathRequestMatcher(FILTER_URL, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if (postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }

        // 手机号
        String mobile = obtainMobile(request);
        if (StringUtils.isEmpty(mobile)) {
            throw new AuthenticationServiceException("手机号不能为空");
        }
        // 验证码
        String code = obtainCode(request);
        if (StringUtils.isEmpty(code)) {
            throw new AuthenticationServiceException("验证码不能为空");
        }
        SmsAuthenticationToken authRequest = new SmsAuthenticationToken(mobile, code);
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Nullable
    protected String obtainMobile(HttpServletRequest request) {
        return request.getParameter(MOBILE_PARAMETER);
    }

    @Nullable
    protected String obtainCode(HttpServletRequest request) {
        return request.getParameter(CODE_PARAMETER);
    }
}
