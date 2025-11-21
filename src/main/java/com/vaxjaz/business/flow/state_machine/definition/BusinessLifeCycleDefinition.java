package com.vaxjaz.business.flow.state_machine.definition;


import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import com.vaxjaz.business.flow.state_machine.guard.Guard;
import com.vaxjaz.business.flow.state_machine.handler.BusinessHandler;

import java.util.List;

/**
 * 业务生命周期定义接口
 *
 * 该接口定义了特定业务类型的完整生命周期，包括：
 * - 业务类型标识
 * - 业务动作（Action）与处理器（Handler）的映射关系
 * - 每个动作对应的守卫（Guard）列表
 *
 * 通过实现此接口，可以声明式地定义业务流程的状态转换规则。
 * 通常使用 BusinessLifeCycleDefinitionBuilder 来构建实现类的实例。
 *
 * 使用示例：
 * <pre>
 * BusinessLifeCycleDefinition<Input, Output> definition =
 *     BusinessLifeCycleDefinitionBuilder.forType(BusinessType.ORDER)
 *         .when(BusinessAction.CREATE, createHandler)
 *             .guard(authGuard)
 *             .next()
 *         .when(BusinessAction.UPDATE, updateHandler)
 *             .next()
 *         .build();
 * </pre>
 *
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
public interface BusinessLifeCycleDefinition<T, R> {

    /**
     * 获取业务类型
     *
     * 返回该生命周期定义对应的业务类型标识。
     * 业务引擎会根据此类型来查找对应的生命周期定义。
     *
     * @return 业务类型枚举
     */
    BusinessType businessType();


    /**
     * 根据业务动作获取对应的业务处理器
     *
     * 该方法定义了业务动作与处理器之间的映射关系。
     * 当业务引擎需要执行某个动作时，会调用此方法获取对应的处理器。
     *
     * @param actionEnums 业务动作枚举
     * @return 对应的业务处理器，如果未找到则抛出异常
     * @throws IllegalArgumentException 当指定的 action 没有对应的处理器时
     */
    BusinessHandler<T, R> getHandlerByAction(BusinessAction actionEnums);


    /**
     * 获取业务动作对应的守卫列表
     *
     * 守卫（Guard）用于在执行业务处理器之前进行条件判断，例如：
     * - 权限校验
     * - 状态检查
     * - 前置条件验证
     *
     * 如果任何一个守卫返回 false，则业务流程会被中止。
     *
     * @param actionEnums 业务动作枚举
     * @return 守卫列表，如果没有守卫则返回 null 或空列表
     */
    List<Guard<T, R>> getActionGuard(BusinessAction actionEnums);

}
