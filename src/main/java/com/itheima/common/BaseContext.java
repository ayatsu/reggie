package com.itheima.common;

/**
 * 基于threadLocal封装的工具类，用于读写当前登录用户的id
 */
public class BaseContext {
    //long因为是id是long
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
