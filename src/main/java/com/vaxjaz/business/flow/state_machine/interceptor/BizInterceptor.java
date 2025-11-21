package com.vaxjaz.business.flow.state_machine.interceptor;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;

/**
 * 业务拦截器接口 - 实现横切关注点的处理
 *
 * 拦截器用于在业务处理前后执行通用逻辑，支持关注点分离。
 * 典型应用场景：
 * - 日志记录和审计
 * - 性能监控和统计
 * - 事务管理
 * - 权限验证
 * - 参数校验和转换
 * - 缓存处理
 *
 * 执行顺序：
 * - beforeTransition 方法按 order 从小到大顺序执行（FIFO）
 * - afterTransition 方法按 order 从大到小顺序执行（LIFO）
 *
 * 示例：
 * <pre>
 * &#64;Component
 * public class LogInterceptor implements BizInterceptor {
 *     public void beforeTransition(BusinessContext context) {
 *         log.info("业务开始: {}", context.getBusinessId());
 *     }
 *
 *     public void afterTransition(BusinessContext context, Object result) {
 *         log.info("业务完成: {}, 结果: {}", context.getBusinessId(), result);
 *     }
 *
 *     public int order() {
 *         return 1;  // order 越小越先执行 before 方法
 *     }
 * }
 * </pre>
 *
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
public interface BizInterceptor<T, R> {

    /**
     * 业务处理前的拦截方法
     *
     * 在业务处理器执行之前被调用，可用于：
     * - 记录开始日志
     * - 初始化资源
     * - 前置校验
     * - 开始计时
     * - 设置线程上下文
     *
     * @param request 业务上下文，包含请求参数和业务信息
     */
    void beforeTransition(BusinessContext<T, R> request);

    /**
     * 业务处理后的拦截方法
     *
     * 在业务处理器执行成功后被调用，可用于：
     * - 记录完成日志
     * - 清理资源
     * - 结束计时并统计
     * - 清理线程上下文
     * - 发送通知
     *
     * 注意：该方法按照与 beforeTransition 相反的顺序执行（LIFO）
     *
     * @param request 业务上下文
     * @param result 业务处理结果
     */
    void afterTransition(BusinessContext<T, R> request, R result);

    /**
     * 定义拦截器的执行顺序
     *
     * order 值越小，beforeTransition 越先执行，afterTransition 越后执行。
     * 默认值为 -1。
     *
     * 执行顺序示例：
     * - 拦截器 A (order=1), B (order=2), C (order=3)
     * - before 执行顺序: A -> B -> C
     * - after 执行顺序: C -> B -> A
     *
     * @return 排序值，越小越先执行
     */
    default int order() {
        return -1;
    }

}
