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

/**
 * 业务处理器包装器 - 实现拦截器链的执行逻辑
 *
 * 该类将业务处理器和拦截器链组合在一起，按照特定顺序执行：
 * 1. 执行所有拦截器的 beforeTransition（FIFO 顺序）
 * 2. 执行处理器的 onEnter 钩子
 * 3. 执行处理器的核心业务逻辑
 * 4. 执行处理器的 onCompleted 钩子
 * 5. 执行所有拦截器的 afterTransition（LIFO 顺序，与 before 相反）
 *
 * 拦截器执行顺序示例：
 * <pre>
 * 假设有拦截器 A、B、C（按 order 排序）
 * 执行顺序：
 * A.before -> B.before -> C.before
 * -> Handler.onEnter -> Handler.execute -> Handler.onCompleted
 * -> C.after -> B.after -> A.after
 * </pre>
 *
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
@Slf4j
@RequiredArgsConstructor
@Data
public class HandlerWrapper<T, R> {

    /** 被包装的目标业务处理器 */
    private final BusinessHandler<T, R> proxy;

    /** 拦截器列表，已按 order 排序 */
    private final List<BizInterceptor<T, R>> interceptors;

    /**
     * 执行完整的处理流程（包含拦截器链）
     *
     * 该方法协调拦截器和处理器的执行顺序，确保正确的调用链。
     * 使用栈来保证拦截器的 after 方法以 LIFO 顺序执行。
     *
     * @param ctx 业务上下文
     * @return 业务处理结果
     * @throws Exception 如果处理过程中发生异常
     */
    public R execute(BusinessContext<T, R> ctx) {
        R result = null;

        // 使用栈来记录已执行的拦截器，保证 after 方法按 LIFO 顺序执行
        Deque<BizInterceptor<T, R>> executedInterceptors = new ConcurrentLinkedDeque<>();

        try {
            // 步骤1: 按 FIFO 顺序执行所有拦截器的 beforeTransition 方法
            if (!CollectionUtils.isEmpty(interceptors)) {
                interceptors.forEach(interceptor -> {
                    interceptor.beforeTransition(ctx);
                    // 将已执行的拦截器压入栈，用于后续的 after 调用
                    executedInterceptors.push(interceptor);
                });
            }

            // 步骤2: 执行处理器的进入钩子
            proxy.onEnter(ctx, result);

            // 步骤3: 执行核心业务逻辑
            result = proxy.execute(ctx);

            // 步骤4: 执行处理器的完成钩子
            proxy.onCompleted(ctx, result);

            // 步骤5: 按 LIFO 顺序执行所有拦截器的 afterTransition 方法
            // 从栈中弹出拦截器，保证执行顺序与 before 相反
            while (!executedInterceptors.isEmpty()) {
                BizInterceptor<T, R> interceptor = executedInterceptors.pop();
                interceptor.afterTransition(ctx, result);
            }
        } catch (Exception e) {
            // 记录异常并重新抛出，让上层处理
            log.warn("业务处理过程中发生异常: {}", e.getMessage(), e);
            throw e;
        }

        return result;
    }
}
