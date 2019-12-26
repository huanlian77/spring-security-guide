package com.liuqs.spring.security.oauth2.uaa.config;

import com.liuqs.spring.security.oauth2.uaa.service.StoreAccessTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.sql.DataSource;

/**
 * 授权服务配置
 */
@Configuration
@EnableAuthorizationServer
@Slf4j
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    /**
     * 注入 AuthenticationManager, 在 WebSecurityConfig 中创建的 Bean
     */
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private DataSource dataSource;
    /**
     * 注入 UserDetailsService，在 refresh_token 时需要
     */
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private StoreAccessTokenService storeAccessTokenService;

    /**
     * 客户端配置, 读取 oauth_client_details 信息
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());
    }

    /**
     * 配置授权，令牌端点配置及令牌服务
     *
     * @param endpoints
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)
                .accessTokenConverter(accessTokenConverter())
                .tokenStore(tokenStore())
                .userDetailsService(userDetailsService)
                .exceptionTranslator(loggingExceptionTranslator());
    }


    /**
     * 配置令牌端点的安全约束
     *
     * @param oauthServer
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
                .tokenKeyAccess("permitAll()") //url:/oauth/token_key,exposes public key for token verification if using JWT tokens
                .checkTokenAccess("permitAll()") //url:/oauth/check_token allow check token
                // 允许表单认证
                .allowFormAuthenticationForClients();
    }


    /**
     * token存储方式
     *
     * @return
     */
    private TokenStore tokenStore() {
        return new CustomJwtTokenStore(accessTokenConverter());
    }

    /**
     * 自定义 JwtTokenStore
     */
    public class CustomJwtTokenStore extends JwtTokenStore {


        public CustomJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
            super(jwtTokenEnhancer);
        }

        /**
         * 存储token
         *
         * @param token
         * @param authentication
         */
        @Override
        public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
            storeAccessTokenService.store(token.getValue(), token.getRefreshToken().getValue());
        }

        @Override
        public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
            int row = storeAccessTokenService.refresh(refreshToken.getValue());
            if (row != 1) {
                throw new InvalidRequestException("refreshToken is invalid!");
            }
        }

    }


    /**
     * token转换器，并设置加密方式
     *
     * @return
     */
    private JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 非对称加密使用：https://blog.csdn.net/AaronSimon/article/details/84071811
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(new ClassPathResource("mytest.jks"), "mypass".toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("mytest"));
        return converter;
    }

    /**
     * 使用 JdbcClientDetailsService, 并设置 DataSource 数据源
     *
     * @return
     */
    private ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    private WebResponseExceptionTranslator<OAuth2Exception> loggingExceptionTranslator() {
        return new DefaultWebResponseExceptionTranslator() {
            @Override
            public ResponseEntity<OAuth2Exception> translate(Exception e) throws Exception {
                //异常堆栈信息输出
                log.error("异常堆栈信息", e);
                return super.translate(e);
            }
        };
    }
}