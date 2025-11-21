package com.vaxjaz.business.flow.state_machine.handler;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;
import com.vaxjaz.business.flow.state_machine.interceptor.BizInterceptor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Slf4j
@RequiredArgsConstructor
@Data
public class HandlerWrapper<T, R> {

    private final BusinessHandler<T, R> proxy;

    private final List<BizInterceptor<T, R>> interceptors;

    public R execute(BusinessContext<T, R> ctx) {
        R result = null;
        // 使用栈模拟先进后出
        Deque<BizInterceptor<T, R>> executedInterceptors = new ConcurrentLinkedDeque<>();
        try {
            // before先进先出，FIFO
            if (!CollectionUtils.isEmpty(interceptors)) {
                interceptors.forEach(workOrderInterceptor -> {
                    workOrderInterceptor.beforeTransition(ctx);
                    executedInterceptors.push(workOrderInterceptor);
                });
            }
            proxy.onEnter(ctx, result);
            result = proxy.execute(ctx);
            proxy.onCompleted(ctx, result);
            // after后进先出,LIFO
            while (!executedInterceptors.isEmpty()) {
                BizInterceptor<T, R> interceptor = executedInterceptors.pop();
                interceptor.afterTransition(ctx, result);
            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw e;
        }
        return result;
    }
}
