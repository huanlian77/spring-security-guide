# 整合 OAuth2.0
OAuth2.0 的介绍推荐阮一峰的：[OAuth 2.0 的一个简单解释](http://www.ruanyifeng.com/blog/2019/04/oauth_design.html)和[OAuth 2.0 的四种方式](http://www.ruanyifeng.com/blog/2019/04/oauth-grant-types.html)。本篇将介绍 Spring Security 整合 OAuth2。代码分为两块：[授权服务](https://github.com/huanlian77/spring-security-guide/tree/master/spring-security-oauth2-uaa)和[资源服务](https://github.com/huanlian77/spring-security-guide/tree/master/spring-security-oauth2-resource-order)。授权服务利用 Spring Security OAuth2.0 完成**访问安全、客户端信息、授权方式等配置**，资源服务利用 Spring Security OAuth2.0 完成**资源模块权限验证**。

## 授权服务
### WebSecurityConfig
```java
@EnableWebSecurity <1>
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http    .csrf()
                .disable()
                // 所有访问需要认证
                .authorizeRequests()
                .anyRequest().authenticated()
                // 登录页
                .and()
                .formLogin()
                .permitAll();
    }

    @Bean
    protected UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean   <2>
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean   <3>
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
```
<1> 添加 @EnableWebSecurity ，开启 Spring Security。<br>
<2> 不使用密码加密。<br>
<3> 创建 AuthenticationManager Bean，在授权服务配置时需要注入。<br>
**注**：UserDetailsService 是自定义的，重写 loadUserByUsername(String) ，详细见代码。

### AuthorizationServerConfig
```Java
@Configuration
@EnableAuthorizationServer <1>
@Slf4j
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {


    @Autowired <2>
    private AuthenticationManager authenticationManager;  
    @Autowired
    private DataSource dataSource;
    @Autowired <3>
    private UserDetailsService userDetailsService;
    @Autowired
    private StoreAccessTokenService storeAccessTokenService;

    @Override <4>
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());<4-1>
    }

    @Override <5>
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(authenticationManager)   <5-1>
                .accessTokenConverter(accessTokenConverter())   <5-2>
                .tokenStore(tokenStore())                       <5-3>
                .userDetailsService(userDetailsService)         <5-4>
                .exceptionTranslator(loggingExceptionTranslator());  <5-5>
    }

    @Override <6>
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer
                .tokenKeyAccess("permitAll()") //url:/oauth/token_key,exposes public key for token verification if using JWT tokens
                .checkTokenAccess("permitAll()") //url:/oauth/check_token allow check token
                // 允许表单认证
                .allowFormAuthenticationForClients();  <6-1>
    }


    private TokenStore tokenStore() {
        return new CustomJwtTokenStore(accessTokenConverter());
    }

    <7>
    public class CustomJwtTokenStore extends JwtTokenStore {
        public CustomJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
            super(jwtTokenEnhancer);
        }

        @Override    <7-1>
        public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
            storeAccessTokenService.store(token.getValue(), token.getRefreshToken().getValue());
        }

        @Override   <7-2>
        public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
            int row = storeAccessTokenService.refresh(refreshToken.getValue());
            if (row != 1) {
                throw new InvalidRequestException("refreshToken is invalid!");
            }
        }
    }

    <8>
    private JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 非对称加密使用：https://blog.csdn.net/AaronSimon/article/details/84071811
        KeyStoreKeyFactory keyStoreKeyFactory =
                new KeyStoreKeyFactory(new ClassPathResource("mytest.jks"), "mypass".toCharArray());
        converter.setKeyPair(keyStoreKeyFactory.getKeyPair("mytest"));
        return converter;
    }

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
```
<1> 添加 @EnableAuthorizationServer，开启 Spring Security OAuth2 授权服务。<br>
<2> 注入 AuthenticationManager，在 <4-1> 设置到 endpoints 中，**密码式(password)** 授权方式时需要用到。<br>
<3> 注入 UserDetailsService，在 <4-4> 设置到 endpoint 中，在 **刷新令牌(refresh token)** 时需要用到。如果不设置，在使用刷新令牌功能会报错。<br>
<4> 客户端配置，这里使用数据库来保存。<br>
<span style="margin-right:30px"></span><4-1> 查看 JdbcClientDetailsService 源码，可以看见该类操作表 `oauth_client_details`，在数据库中创建表 `oauth_client_details`。
```sql
DROP TABLE IF EXISTS `oauth_client_details`;
CREATE TABLE `oauth_client_details` (
  `client_id` varchar(48) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```
<5> 令牌端点配置。<br>
<span style="margin-right:30px"></span><5-1> 设置 AuthenticationManager，在 **密码式(password)** 授权方式时需要用到。<br>
<span style="margin-right:30px"></span><5-2> 设置 AccessTokenConverter，这里使用 JwtAccessTokenConverter，在第 <8> 步中介绍。<br>
<span style="margin-right:30px"></span><5-3> 设置 TokenStore，这里使用 JwtTokenStore，在第 <7> 步中介绍。<br>
<span style="margin-right:30px"></span><5-4> 设置 UserDetailsService，在 **刷新令牌(refresh token)** 时需要用到。<br>
<span style="margin-right:30px"></span><5-5> OAuth2Exception 异常打印。<br>
<6> 配置令牌端点的安全约束。<br>
<span style="margin-right:30px"></span><6-1> 允许表单认证。 <br>
<7> 自定义 JwtTokenStore。 <br>

**为什么需要自定义 JwtTokenStore 呢？**
查看 JwtTokenStore 源码：

```Java
public class JwtTokenStore implements TokenStore {
  ....
    @Override
  	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {}
  ...
}
```
发现 JwtTokenStore#removeAccessTokenUsingRefreshToken(OAuth2RefreshToken) 是一个空方法，会造成使用 refreshToken 刷新 token 过，旧 token 依旧可用。如果要实现使用 refreshToken 刷新 token 过，旧 toke 不能使用，那么需要自定义 JwtTokenStore。
<span style="margin-right:30px"></span><7-1> 使用数据库保存 token 和 refreshToken。 <br>
<span style="margin-right:30px"></span><7-2> 但刷新 token 时，通过 refreshToken 删除数据库记录。 <br>
<8> Jwt 转换，使用非对称加密。通过 `keytool -genkeypair -alias mytest -keyalg RSA -keypass mypass -keystore mytest.jks -storepass mypass` 生成 `mytest.jks` 文件，再通过 `keytool -list -rfc --keystore mytest.jks | openssl x509 -inform pem -pubkey` 获取公、私钥，并把公钥保存咋 `public.text` 文件。

## 资源服务
### ResourceServerConfig
```Java
@Configuration
@EnableResourceServer   
@EnableGlobalMethodSecurity(prePostEnabled = true) <1>
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private StoreAccessTokenService storeAccessTokenService;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable()
                .authorizeRequests()
                .anyRequest().authenticated();
    }

    @Override  <2>
    public void configure(ResourceServerSecurityConfigurer config) {
        config.tokenServices(tokenServices()) <2-1>
                .resourceId("order"); <2-2>
    }

    @Bean   <3>
    public TokenStore tokenStore() {
        return new CustomJwtTokenStore(accessTokenConverter());
    }

    @Bean   <4>
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        Resource resource = new ClassPathResource("public.txt");
        String publicKey;
        try {
            publicKey = inputStream2String(resource.getInputStream());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        converter.setVerifierKey(publicKey);
        return converter;
    }

    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }

    private String inputStream2String(InputStream is) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public class CustomJwtTokenStore extends JwtTokenStore {
        public CustomJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer) {
            super(jwtTokenEnhancer);
        }

        @Override
        public OAuth2AccessToken readAccessToken(String tokenValue) {
            AccessTokenE.TokenStoreStatus tokenStoreStatus = storeAccessTokenService.read(tokenValue);
            if (tokenStoreStatus == null || AccessTokenE.TokenStoreStatus.refresh.equals(tokenStoreStatus)) {
                throw new InvalidTokenException("token is invalid!");
            }
            return super.readAccessToken(tokenValue);
        }
    }
}
```
<1> 添加 @EnableResourceServer 开启 Spring Security OAuth2 授权服务，添加 @EnableGlobalMethodSecurity(prePostEnabled = true) 开启方法权限校验。<br>
<2> 资源服务 OAuth2.0 配置。<br>
<span style="margin-right:30px"></span><2-1> 设置令牌服务，令牌服务中指定 tokenStore，tokenStore 使用的**自定义的 JwtTokenStore。当 refreshToken 刷新 token 过，通过旧 token 访问时会提示
token is invalid!**<br>
<span style="margin-right:30px"></span><2-2> 指定资源id，对应表 `oauth_client_details` 中  resource_ids 字段。只有资源id对应才能访问。<br>
<3> 自定义 JwtTokenStore。<br>
<4> 通过公钥验证 token。

## 运行

### 授权码模式
1. 浏览器访问 <http://localhost:8080/oauth/authorize?client_id=123&redirect_uri=http://baidu.com&response_type=code&scope=order>，数据库中客户端信息 client_id为123，redirect_uri为http://baidu.com，scope为order。这时会跳转到登录页面：

![授权码-登录](https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114900.jpg)

2. 输入用户名密码，然后点击 **Authorize** 进行授权：

![授权码-授权](https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114855.jpg)


4. 3. 授权后跳转到 <http://baidu.com>，得到授权码 code：

![授权码-得到code](https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114823.jpg)

4. 获取 token：

<img src="https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114819.jpg" alt="授权码-token" style="zoom:80%;" />

5. 访问 `/sayHello`：

![授权码-访问sayHello](https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114813.jpg)

### 密码模式

<img src="https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114806.jpg" alt="密码-token" style="zoom:80%;" />

### 刷新 token

<img src="https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114800.jpg" alt="刷新token" style="zoom:80%;" />