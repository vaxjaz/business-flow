package com.vaxjaz.business.flow.state_machine.core;

import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.anno.BizLock;
import com.vaxjaz.business.flow.state_machine.anno.HandlerInterceptors;
import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;
import com.vaxjaz.business.flow.state_machine.definition.BusinessDefRegistry;
import com.vaxjaz.business.flow.state_machine.definition.BusinessLifeCycleDefinition;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import com.vaxjaz.business.flow.state_machine.exception.SysEx;
import com.vaxjaz.business.flow.state_machine.guard.Guard;
import com.vaxjaz.business.flow.state_machine.handler.BusinessHandler;
import com.vaxjaz.business.flow.state_machine.handler.HandlerWrapper;
import com.vaxjaz.business.flow.state_machine.interceptor.BizInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 业务流程引擎 - 框架的核心执行组件
 *
 * 职责：
 * 1. 接收业务请求并构建上下文
 * 2. 查找对应的生命周期定义和处理器
 * 3. 执行守卫（Guard）检查，判断是否允许执行
 * 4. 包装拦截器（Interceptor）到处理器
 * 5. 执行业务处理器并返回结果
 *
 * 该类是整个状态机框架的入口，通过声明式配置实现业务流程的统一管理
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessEngine {

    /** 业务定义注册器，用于查找业务类型对应的生命周期定义 */
    private final BusinessDefRegistry registry;

    /** Spring 应用上下文，用于获取拦截器 Bean 实例 */
    private final ApplicationContext applicationContext;

    /**
     * 业务流程处理核心方法
     *
     * 该方法实现了完整的业务流程处理逻辑，包括守卫检查、拦截器执行、业务处理等步骤
     *
     * @param bizId 业务唯一标识，用于追踪和定位具体业务实例
     * @param businessType 业务类型，决定使用哪个生命周期定义
     * @param action 业务动作，决定执行哪个处理器
     * @param request 业务请求参数，泛型类型 T
     * @param <T> 请求参数类型
     * @param <R> 返回结果类型
     * @return 业务处理结果
     * @throws RuntimeException 当守卫检查不通过或处理器执行失败时抛出异常
     */
    @SuppressWarnings("unchecked")
    @BizLock()  // 业务级别的分布式锁，防止并发问题
    public <T, R> R process(String bizId,
                            BusinessType businessType,
                            BusinessAction action,
                            T request) {
        // 步骤1: 构建业务上下文，封装本次请求的所有必要信息
        BusinessContext<T, R> ctx = BusinessContext.ofNew(bizId, businessType, action, request);

        // 步骤2: 从注册器中获取业务类型对应的生命周期定义
        BusinessLifeCycleDefinition<T, R> def = registry.getDefinition(businessType);

        // 步骤3: 根据 action 获取对应的业务处理器（内含所有步骤和生命周期回调）
        BusinessHandler<T, R> handler = def.getHandlerByAction(action);

        // 步骤4: 获取并执行守卫（Guard）检查，判断当前上下文是否允许执行该动作
        List<Guard<T, R>> actionGuard = def.getActionGuard(action);
        if (!CollectionUtils.isEmpty(actionGuard)) {
            try {
                // 遍历所有守卫，任何一个守卫不通过都会抛出异常
                actionGuard.forEach(trGuard -> {
                    if (!trGuard.canProceed(ctx)) {
                        SysEx.throwDirect(String.format("def [%s] Action [%s] not allow by guard !", def.businessType(), action));
                    }
                });
            } catch (Exception e) {
                log.warn("守卫校验异常 msg {} bizId 【{}】 businessType 【{}】 action 【{}】", e.getMessage(), bizId, businessType, action, e);
                throw e;
            }
        }

        // 步骤5: 包装处理器，将拦截器链组装到处理器上
        HandlerWrapper<T, R> wrap = wrap(handler);

        // 步骤6: 执行包装后的处理器，按顺序执行拦截器和业务逻辑
        log.info("状态机启动 {}", ctx);
        R rawResult = wrap.execute(ctx);

        // 步骤7: 将执行结果写回业务上下文，方便后续追踪
        ctx.setResp(rawResult);
        log.info("状态机完成 {}", ctx);

        // 步骤8: 返回业务处理结果给调用方
        return rawResult;
    }

    /**
     * 包装业务处理器，组装拦截器链
     *
     * 该方法会：
     * 1. 查找处理器上的 @HandlerInterceptors 注解
     * 2. 从 Spring 容器中获取拦截器实例
     * 3. 按照拦截器的 order 进行排序
     * 4. 创建 HandlerWrapper 包装器，将拦截器和处理器组合在一起
     *
     * @param targetHandler 目标业务处理器
     * @param <T> 请求参数类型
     * @param <R> 返回结果类型
     * @return 包装后的处理器，包含拦截器链
     */
    private <T, R> HandlerWrapper<T, R> wrap(BusinessHandler<T, R> targetHandler) {
        // 获取处理器类上的 @HandlerInterceptors 注解
        HandlerInterceptors annotation = targetHandler.getClass().getAnnotation(HandlerInterceptors.class);
        List<BizInterceptor<T, R>> interceptors = new ArrayList<>();

        // 如果注解存在，则从 Spring 容器中获取所有声明的拦截器实例
        if (Objects.nonNull(annotation)) {
            for (Class<? extends BizInterceptor<T, R>> clzz : annotation.value()) {
                BizInterceptor<T, R> bean = applicationContext.getBean(clzz);
                interceptors.add(bean);
            }
        }

        // 按照拦截器的 order 值进行排序，order 值越小越先执行
        interceptors = interceptors.stream().sorted(Comparator.comparing(BizInterceptor::order)).toList();

        // 创建并返回包装器，包含目标处理器和拦截器链
        return new HandlerWrapper<>(targetHandler, interceptors);
    }
}
