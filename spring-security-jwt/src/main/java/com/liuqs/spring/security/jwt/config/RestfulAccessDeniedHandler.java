package com.liuqs.spring.security.jwt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuqs.spring.security.jwt.constant.ResultConstant;
import com.liuqs.spring.security.jwt.vo.Result;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 当访问接口没有权限时，自定义的返回结果
 */
public class RestfulAccessDeniedHandler implements AccessDeniedHandler{
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        Result error = Result.error(ResultConstant.PERMISSION_DENIED);
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter writer = response.getWriter();
        writer.write(mapper.writeValueAsString(error));
        writer.flush();
        writer.close();
    }
}
