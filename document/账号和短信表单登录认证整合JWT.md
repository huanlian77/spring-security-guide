# 整合JWT

JWT 的介绍推荐阮一峰的：[JSON Web Token 入门教程](http://www.ruanyifeng.com/blog/2018/07/json_web_token-tutorial.html)。本篇将介绍 Spring Security 整合 JWT，代码见：[Spring Security 整合 JWT](https://github.com/huanlian77/spring-security-guide/tree/master/spring-security-jwt)

## 思路

第一步：定义 JWT 工具类，用于生成 token、从 token 获取用户信息。<br>
第一步：使用 **短信验证码或账号密码** 进行登录，成功登录返回 token。<br>
第二步: 定义 JWT 过滤器，判断请求头中是否有 JWT 负载。有时，从 token 中获取用户信息封装成 UsernamePasswordAuthenticationToken，并添加到 SecurityContext中 与 ThreadLocal 进行绑定。<br>

## JwtTokenUtil
```Java
public class JwtTokenUtil {

    private static String CLAIM_KEY_USERNAME = "subject";
    private static String CLAIM_KEY_CREATED = "created";
    private static String CLAIM_KEY_AUTHORITY = "authority";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成token
     *
     * @param userDetails
     * @return
     */
    public Token generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
        claims.put(CLAIM_KEY_CREATED, new Date());
        claims.put(CLAIM_KEY_AUTHORITY, authorities2Str(userDetails.getAuthorities()));

        String token = Jwts.builder() (1)
                .setClaims(claims)
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        return new Token().setToken(token).setTokenHead(tokenHead);
    }


    private String authorities2Str(Collection<? extends GrantedAuthority> authorities) {
        List<String> authorities2StrList = authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return String.join(",", authorities2StrList);
    }

    /**
     * 生成token的过期时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 根据token获取用户名
     *
     * @param token
     * @return
     */
    public String getUserNameByToken(String token) {(2)
        Claims claims =  Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        return (String) claims.get(CLAIM_KEY_USERNAME);
    }


    /**
     * 根据token获取权限
     *
     * @param token
     * @return
     */
    public List<SimpleGrantedAuthority> getGrantedAuthorityByToken(String token) {(3)
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
        String authorities = (String) claims.get(CLAIM_KEY_AUTHORITY);
        return Arrays.stream(authorities.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
```
(1) 设置生成 token 所需要的属性，Claims 是一个 Map 集合，用于存储用户信息。<br>
(2) 获取 Claims 对象，从中获取用户名。<br>
(3) 获取 Claims 对象，从中获取用户权限。<br>

## 短信验证码或者账号密码登录
```Java
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public Token sms(LoginReqVo reqVo) { (1)
        // 1. 从 redis 或者 数据库中获取验证码
        String mobile = reqVo.getMobile();
        String storageCode = findByMobile(mobile);
        if (!storageCode.equals(reqVo.getCode())) {
            throw new BadCredentialsException("验证码不正确!");
        }
        // 2. 获取用户信息
        UserDetails userDetails = userService.findByMobile(mobile);
        if (userDetails == null) {
            throw new BadCredentialsException("手机号不存在!");
        }
        // 3. 获取token
        return jwtTokenUtil.generateToken(userDetails);(2)
    }

    @Override
    public Token passport(LoginReqVo reqVo) { (3)
        // 1. 获取用户信息
        UserDetails userDetails = userService.findByUsername(reqVo.getUsername());
        if (!userDetails.getPassword().equals(reqVo.getPassword())) {
            throw new BadCredentialsException("密码不正确!");
        }
        if (userDetails == null) {
            throw new BadCredentialsException("用户不存在!");
        }
        // 2. 获取token
        return jwtTokenUtil.generateToken(userDetails);(4)
    }

    /**
     * 通过手机号查询验证码
     * <p>
     * 假设从 redis 或者 数据库 中获取到验证码 1234
     *
     * @param mobile
     * @return
     */
    private String findByMobile(String mobile) {
        return "1234";
    }
```
(1) 短信验证码登录，根据手机号从 redis 或者 数据库 等获取验证码，把验证码与用户输入的验证码进行对比，然后根据手机号查找用户信息。<br>
(3) 账号密码登录，根据用户名查找用户信息。<br>
(2)、(4) 找到用户信息后，调用 JWT 工具类用于生成 token。<br>

## JwtAuthorizationFilter
```Java
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
            // 根据 token 获取用户名 (1)
            String username = jwtTokenUtil.getUserNameByToken(token);
            // 根据 token 获取权限
            List<SimpleGrantedAuthority> authorityList = jwtTokenUtil.getGrantedAuthorityByToken(token);(2)
            // 存在用户名，封装成 UsernamePasswordAuthenticationToken，并把 SecurityContext 添加到 ThreadLocal 中
            if (username != null) {
                (3)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorityList);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
```
(1) 根据 token 获取用户名。<br>
(2) 根据 token 获取用户权限。<br>
(3) 把用户信息封装成 UsernamePasswordAuthenticationToken，并添加到 SecurityContext中 与 ThreadLocal 进行绑定。<br>
## 配置
```Java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled=true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/login/sms","/login/passport").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .cors()
                // // 关闭跨站请求防护及不使用session
                .and()
                .csrf()
                .disable()
                .headers()
                .frameOptions()
                .disable()
                .disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .accessDeniedHandler(restfulAccessDeniedHandler())
                .authenticationEntryPoint(restfulAuthenticationEntryPoint())
                // 自定义拦截器
                .and()
                .addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() throws Exception {
        return new JwtAuthorizationFilter(authenticationManager());
    }

    @Bean
    public RestfulAuthenticationEntryPoint restfulAuthenticationEntryPoint(){
        return new RestfulAuthenticationEntryPoint();
    }

    @Bean
    public RestfulAccessDeniedHandler restfulAccessDeniedHandler(){
        return new RestfulAccessDeniedHandler();
    }

    @Bean
    public JwtTokenUtil jwtTokenUtil(){
        return new JwtTokenUtil();
    }
}
```
## 运行
登录：

<img src="https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114423.jpg" alt="JWT登录" style="zoom: 80%;" />

访问 `/sayHi`：

![JWT访问sayHi](https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114428.jpg)

访问 `/sayHello`：

![JWT访问sayHello](https://cdn.jsdelivr.net/gh/huanlian77/CDN/images/20200609114433.jpg)

