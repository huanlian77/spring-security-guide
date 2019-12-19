package com.liuqs.spring.security.normal.config;

import com.liuqs.spring.security.normal.security.UserDetailsServiceImpl;
import com.liuqs.spring.security.normal.security.passport.PassportAuthenticationProcessingFilter;
import com.liuqs.spring.security.normal.security.passport.PassportAuthenticationProvider;
import com.liuqs.spring.security.normal.security.sms.SmsAuthenticationProcessingFilter;
import com.liuqs.spring.security.normal.security.sms.SmsAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll()
                .and()
                .cors()
                // 关闭跨站请求防护
                .and()
                .csrf()
                .disable()
                .headers()
                .frameOptions()
                .disable()
                // 自定义权限拒绝处理类
                .and()
                .exceptionHandling()
                .accessDeniedHandler(restfulAccessDeniedHandler())
                // 自定义拦截器
                .and()
                .addFilterBefore(passportAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(smsAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AbstractAuthenticationProcessingFilter passportAuthenticationProcessingFilter() throws Exception {
        PassportAuthenticationProcessingFilter passportAuthenticationProcessingFilter = new PassportAuthenticationProcessingFilter();
        passportAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        passportAuthenticationProcessingFilter.setAuthenticationFailureHandler(myAuthenticationFailureHandler());
        passportAuthenticationProcessingFilter.setAuthenticationSuccessHandler(myAuthenticationSuccessHandler());
        return passportAuthenticationProcessingFilter;
    }

    @Bean
    public AbstractAuthenticationProcessingFilter smsAuthenticationProcessingFilter() throws Exception {
        SmsAuthenticationProcessingFilter smsAuthenticationProcessingFilter = new SmsAuthenticationProcessingFilter();
        smsAuthenticationProcessingFilter.setAuthenticationManager(authenticationManager());
        smsAuthenticationProcessingFilter.setAuthenticationFailureHandler(myAuthenticationFailureHandler());
        smsAuthenticationProcessingFilter.setAuthenticationSuccessHandler(myAuthenticationSuccessHandler());
        return smsAuthenticationProcessingFilter;
    }

    @Bean
    public AuthenticationFailureHandler myAuthenticationFailureHandler() {
        return new MyAuthenticationFailureHandler();
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
        return new MyAuthenticationSuccessHandler();
    }

    @Bean
    public AccessDeniedHandler restfulAccessDeniedHandler(){
        return new RestfulAccessDeniedHandler();
    }

    @Bean
    public UserDetailsServiceImpl userDetailsServiceImpl() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public AuthenticationProvider passportAuthenticationProvider() {
        return new PassportAuthenticationProvider();
    }

    @Bean
    public AuthenticationProvider smsAuthenticationProvider() {
        return new SmsAuthenticationProvider();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(passportAuthenticationProvider())
                .authenticationProvider(smsAuthenticationProvider());
    }

}