package com.liuqs.spring.security.jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuqs.spring.security.jwt.constant.ResultConstant;
import com.liuqs.spring.security.jwt.vo.Result;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 当未登录或者token失效访问接口时，自定义的返回结果
 */
public class RestfulAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        Result error = Result.error(ResultConstant.UNAUTHORIZED);
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter writer = response.getWriter();
        writer.println(mapper.writeValueAsString(error));
        writer.flush();
        writer.close();
    }
}
