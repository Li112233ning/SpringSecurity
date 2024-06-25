package com.lnn.filter;

import com.lnn.domain.LoginUser;
import com.lnn.utils.JwtUtil;
import com.lnn.utils.RedisCache;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * 继承 `OncePerRequestFilter` 而不是直接实现 `Filter` 接口有几个主要的优点:
 *
 * 1. 一次性过滤: `OncePerRequestFilter` 确保过滤器只会被执行一次,而不是每次通过过滤器链时都执行。这可以提高性能和避免重复处理。
 *
 * 2. 抽象实现: `OncePerRequestFilter` 提供了一些抽象方法和默认实现,使得编写自定义过滤器更加简单。开发人员只需要实现
 * `doFilterInternal()` 方法,而不必处理整个过滤器生命周期。
 *
 * 3. 异常处理: `OncePerRequestFilter` 会捕获任何在 `doFilterInternal()` 方法中抛出的异常,并将其转发给应用程序的异常处理机制。
 * 这使得过滤器的错误处理更加优雅和一致。
 *
 * 4. 对比原生 Filter: 直接实现 `Filter` 接口需要处理整个过滤器生命周期,包括 `init()`, `doFilter()` 和 `destroy()` 方法。
 * 使用 `OncePerRequestFilter` 可以专注于请求级别的过滤逻辑,减少样板代码。
 *
 * 总的来说,继承 `OncePerRequestFilter` 可以使你的 JWT 身份验证过滤器更加简洁、可靠和易维护。它提供了一些基本功能,
 * 让你可以专注于实现自定义的过滤逻辑,而不必处理整个过滤器生命周期。
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        //获取token
        String token = httpServletRequest.getHeader("token");
        if (!StringUtils.hasText(token)){
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            //不return的话执行万后面的过滤器后，响应回来后会走过滤器链继续执行下面的几个功能
            return;
        }
        String userId;
        //解析token
        try {
            Claims claims = JwtUtil.parseJWT(token);
            //claims.getSubject() 是一个常见的 JWT 解析操作,用于从JWT中提取标识用户或设备的唯一标识符
            userId = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException("token非法");
        }
        //从redis中获取用户信息
        String redisKey = "login:" + userId;
        LoginUser user = redisCache.getCacheObject(redisKey);
        if (Objects.isNull(user)){
            throw new RuntimeException("用户未登录");
        }
        //存入securityContextHolder
        //TODO 获取权限信息封装到Authentication中
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);


        filterChain.doFilter(httpServletRequest,httpServletResponse);

    }
}





















