package com.liuqs.spring.security.jwt.security;

import com.liuqs.spring.security.jwt.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * JWT 授权登录过滤器
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.tokenHead}")
    private String jwtHeader;
    private static final String JWT_HEAD = "Authorization";

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, AuthenticationEntryPoint authenticationEntryPoint) {
        super(authenticationManager, authenticationEntryPoint);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // 获取Jwt请求头负载信息
        String authorization = request.getHeader(JWT_HEAD);
        // Jwt请求头负载信息不为空，并且以Bearer开头
        if (authorization != null && authorization.startsWith(jwtHeader)) {
            String token = authorization.substring(jwtHeader.length() + 1);
            // 根据 token 获取用户名
            String username = jwtTokenUtil.getUserNameByToken(token);
            // 根据 token 获取权限
            List<SimpleGrantedAuthority> authorityList = jwtTokenUtil.getGrantedAuthorityByToken(token);
            // 存在用户名，封装成 UsernamePasswordAuthenticationToken，并把 SecurityContext 添加到 ThreadLocal 中
            if (username != null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorityList);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }

}
