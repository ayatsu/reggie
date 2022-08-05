package com.itheima.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取本次请求url
        String requestURI = request.getRequestURI();

        //定义放行的url
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/login",
                "/user/sendMsg"
        };

        //判断是否需要处理
        boolean isLoginFree = check(requestURI, urls);

        //如果不需要处理直接放行
        if(isLoginFree){
            filterChain.doFilter(request, response);
            log.info("本次请求不需要处理"+requestURI);
            return;
        }

        //（需要登录的部分）如果是登陆则放行
        if(request.getSession().getAttribute("employee") != null){

            //放进thread
            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request, response);
            log.info("本次请求已经登录"+requestURI);
            return;
        }

        if(request.getSession().getAttribute("user") != null){

            //放进thread
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            log.info("本次请求已经登录"+requestURI);
            return;
        }

        //如果没有登录则返回未登录结果(封装成R类
        log.info("本次请求已经拦截"+requestURI);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));//“NOTLOGIN”是前端规定的，在js/request.js
        return;

    }

    /**
     * 检查是否放行
     * @param requestURI
     * @param urls
     * @return
     */
    public boolean check(String requestURI, String[] urls){
        for(String url:urls){
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }


}
