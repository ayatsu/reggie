package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 简易版login（没有短信验证码等
     * @param request
     * @param user
     * @return
     */
    @PostMapping("/login")
    private R<User> login(HttpServletRequest request, @RequestBody User user){
        log.info(user.toString());
        //因为发不了短信所以略
        //正常步骤：
        //获取手机号
        //获取验证码
        //获取session存储中的验证码并对比

        String phone = user.getPhone();
        //如果user中没有这个手机号，则自动注册
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getPhone, phone);
        User one = userService.getOne(qw);
        if(one == null){//是新用户
            one = new User();
            one.setPhone(phone);
            userService.save(one);
        }

        request.getSession().setAttribute("user", one.getId());
        return R.success(one);
    }
}
