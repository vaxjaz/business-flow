package com.vaxjaz.business.flow.state_machine.anno;


import com.vaxjaz.business.flow.state_machine.interceptor.BizInterceptor;

import java.lang.annotation.*;

/**
 * handler业务拦截器
 * 扩展使用
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HandlerInterceptors {

    Class<? extends BizInterceptor>[] value();

}
