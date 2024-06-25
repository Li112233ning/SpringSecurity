package com.lnn.service.impl;

import com.lnn.domain.LoginUser;
import com.lnn.domain.ResponseResult;
import com.lnn.domain.User;
import com.lnn.service.LoginServcie;
import com.lnn.utils.JwtUtil;
import com.lnn.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Objects;

/**
 * AuthenticationManager Authentication进行用户认证
 *  使用方法：SecurityConfig中
 *     @Bean
 *     @Override
 *     public AuthenticationManager authenticationManagerBean() throws Exception {
 *         return super.authenticationManagerBean();
 *     }
 *
 *     然后注入直接使用
 */

@Service
public class LoginServcieImpl implements LoginServcie {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult login(User user) {

        //把用户的用户名和密码封装成authentication对象
        UsernamePasswordAuthenticationToken AuthenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        //传到authenticationManager去认证
        Authentication authenticate = authenticationManager.authenticate(AuthenticationToken);


        if(Objects.isNull(authenticate)){
            throw new RuntimeException("用户名或密码错误");
        }
        //使用userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        //authenticate存入redis
        redisCache.setCacheObject("login:"+userId,loginUser);
        //把token响应给前端
        HashMap<String, String> map = new HashMap<>();
        map.put("token", jwt);

        return new ResponseResult(200, "登陆成功", map);

    }

    @Override
    public ResponseResult logout() {
        //获取SecurityContextHolder中的用户id
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        //删除redis中的值

        redisCache.deleteObject("login:"+userId);
        return new ResponseResult(200,"注销成功");
    }
}
