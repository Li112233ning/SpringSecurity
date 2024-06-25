package com.lnn.controller;

import com.lnn.domain.ResponseResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    @GetMapping("/hello")
    @PreAuthorize("hasAnyAuthority('test')")
    public String hello(){
        return "hello";
    }

}
