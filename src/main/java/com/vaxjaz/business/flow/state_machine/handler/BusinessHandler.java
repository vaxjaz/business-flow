package com.vaxjaz.business.flow.state_machine.handler;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;

/**
 * 业务处理器接口 - 业务逻辑的执行单元
 *
 * 该接口定义了业务处理器的核心方法和生命周期钩子。
 * 每个具体的业务处理器都应该实现此接口，并重写 execute 方法来实现具体的业务逻辑。
 *
 * 生命周期：
 * 1. onEnter() - 进入处理器前执行
 * 2. execute() - 执行核心业务逻辑
 * 3. onCompleted() - 处理完成后执行
 *
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
public interface BusinessHandler<T, R> {

    /**
     * 执行业务逻辑的核心方法（必须实现）
     *
     * 该方法包含了具体业务的处理逻辑，是业务处理器的核心。
     * 实现类需要从 BusinessContext 中获取输入参数，进行业务处理，并返回处理结果。
     *
     * @param context 业务上下文，包含业务ID、业务类型、动作、输入参数等信息
     * @return 业务处理结果，类型为泛型 R
     */
    R execute(BusinessContext<T, R> context);

    /**
     * 进入处理器时的钩子方法（可选实现）
     *
     * 在 execute() 方法执行之前被调用，可用于：
     * - 初始化资源
     * - 记录日志
     * - 前置校验
     * - 设置默认值
     *
     * @param context 业务上下文
     * @param result 此时 result 通常为 null，因为业务逻辑还未执行
     */
    default void onEnter(BusinessContext<T, R> context, R result) {
        // 默认实现为空，子类可选择性重写
    }

    /**
     * 处理完成时的钩子方法（可选实现）
     *
     * 在 execute() 方法执行成功后被调用，可用于：
     * - 清理资源
     * - 记录审计日志
     * - 发送通知
     * - 更新统计信息
     *
     * @param context 业务上下文
     * @param result 业务处理的返回结果
     */
    default void onCompleted(BusinessContext<T, R> context, R result) {
        // 默认实现为空，子类可选择性重写
    }

}
