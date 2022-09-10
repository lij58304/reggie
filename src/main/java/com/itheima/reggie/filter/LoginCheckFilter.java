package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *检查用户登录过滤器
 */

@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        String requestURI = httpServletRequest.getRequestURI();

        //放行白名单
        String[] urls= new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        //检测本次请求是否要处理
        boolean check = check(urls,requestURI);

        //不需要处理放行
        if (check){
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        //判断登录员工状态,已登录,则放行
        if(httpServletRequest.getSession().getAttribute("employee")!=null) {
            Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        //判断登录用户状态,已登录,则放行
        if(httpServletRequest.getSession().getAttribute("user")!=null) {
            Long userId = (Long) httpServletRequest.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));

        return;
    }


    /**
     * 路径匹配,检测是否放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
