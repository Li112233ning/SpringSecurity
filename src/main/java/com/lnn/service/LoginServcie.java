package com.lnn.service;

import com.lnn.domain.ResponseResult;
import com.lnn.domain.User;
import org.springframework.web.bind.annotation.ResponseBody;

public interface LoginServcie {
    ResponseResult login(User user);

    ResponseResult logout();
}
