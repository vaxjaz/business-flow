package com.vaxjaz.business.flow.state_machine.definition;


import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import com.vaxjaz.business.flow.state_machine.guard.Guard;
import com.vaxjaz.business.flow.state_machine.handler.BusinessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.*;

/**
 * 业务生命周期定义构建器
 *
 * 提供流式 API 来构建业务生命周期定义，使用 Builder 模式让配置更加清晰和易读。
 *
 * 使用示例：
 * <pre>
 * BusinessLifeCycleDefinition definition =
 *     BusinessLifeCycleDefinitionBuilder
 *         .forType(BusinessType.ORDER)              // 指定业务类型
 *         .when(BusinessAction.CREATE, handler1)     // 定义动作和处理器
 *             .guard(authGuard, statusGuard)         // 可选：添加守卫
 *             .next()                                // 结束当前动作配置
 *         .when(BusinessAction.UPDATE, handler2)
 *             .next()
 *         .build();                                  // 构建最终的定义对象
 * </pre>
 *
 * 主要功能：
 * - 声明式定义业务流程
 * - 关联动作与处理器
 * - 配置守卫规则
 * - 类型安全的构建过程
 */
@Slf4j
public class BusinessLifeCycleDefinitionBuilder {

    /** 业务类型 */
    private final BusinessType workType;

    /** 动作与处理器的映射关系 */
    private final Map<BusinessAction, BusinessHandler> handlerMap = new EnumMap<>(BusinessAction.class);

    /** 动作与守卫列表的映射关系 */
    private final Map<BusinessAction, List<Guard>> guardMap = new EnumMap<>(BusinessAction.class);

    /**
     * 私有构造方法，通过 forType() 静态方法创建实例
     *
     * @param workType 业务类型
     */
    private BusinessLifeCycleDefinitionBuilder(BusinessType workType) {
        this.workType = workType;
    }

    /**
     * 创建指定业务类型的构建器
     *
     * @param type 业务类型
     * @return 构建器实例
     */
    public static BusinessLifeCycleDefinitionBuilder forType(BusinessType type) {
        return new BusinessLifeCycleDefinitionBuilder(type);
    }

    /**
     * 构建业务生命周期定义对象
     *
     * 根据之前配置的动作、处理器和守卫信息，创建一个不可变的生命周期定义实例。
     *
     * @return 业务生命周期定义对象
     */
    public BusinessLifeCycleDefinition build() {

        return new BusinessLifeCycleDefinition() {
            @Override
            public BusinessType businessType() {
                return workType;
            }

            @Override
            public BusinessHandler getHandlerByAction(BusinessAction actionEnums) {
                return Optional.ofNullable(handlerMap.get(actionEnums))
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format("action %s 没有对应的业务handler", actionEnums)));
            }

            @Override
            public List<Guard> getActionGuard(BusinessAction actionEnums) {
                return guardMap.get(actionEnums);
            }
        };
    }


    /**
     * 定义业务动作与处理器的映射关系
     *
     * 使用 `when` 表示"当某个动作发生时"，会执行对应的处理器。
     * 该方法会检查动作是否重复定义，如果重复会输出警告日志。
     *
     * @param action 业务动作
     * @param handler 对应的业务处理器
     * @return StepBuilder，用于继续配置守卫或进入下一个动作定义
     */
    public StepBuilder when(BusinessAction action, BusinessHandler handler) {
        if (handlerMap.containsKey(action)) {
            log.warn("业务类型 【{}】，状态机定义中 action 【{}】 重复定义", this.workType, action);
        }
        handlerMap.put(action, handler);
        return new StepBuilder(this, action);
    }


    /**
     * 步骤构建器 - 用于配置单个动作的详细信息
     *
     * 该类提供了流式 API 来配置守卫等额外信息，并支持链式调用。
     */
    public static class StepBuilder {

        /** 父构建器引用 */
        private final BusinessLifeCycleDefinitionBuilder parent;

        /** 当前配置的动作 */
        private final BusinessAction action;

        /**
         * 构造步骤构建器
         *
         * @param parent 父构建器
         * @param action 当前动作
         */
        public StepBuilder(BusinessLifeCycleDefinitionBuilder parent, BusinessAction action) {
            this.parent = parent;
            this.action = action;
        }

        /**
         * 为当前动作添加守卫
         *
         * 守卫会在执行处理器之前被调用，用于判断是否允许执行该动作。
         * 多个守卫按照数组顺序依次执行，所有守卫都通过才会继续执行。
         *
         * @param guard 守卫数组，至少需要一个守卫
         * @return 当前 StepBuilder 实例，支持链式调用
         * @throws IllegalArgumentException 如果 guard 为 null
         */
        public StepBuilder guard(Guard... guard) {
            Assert.notNull(guard, "guard 不能为空！");
            parent.guardMap.put(action, Arrays.asList(guard));
            return this;
        }


        /**
         * 结束当前动作的配置，返回父构建器
         *
         * 调用此方法后可以继续配置其他动作，或调用 build() 方法完成构建。
         *
         * @return 父构建器实例
         */
        public BusinessLifeCycleDefinitionBuilder next() {
            return parent;
        }
    }


}
