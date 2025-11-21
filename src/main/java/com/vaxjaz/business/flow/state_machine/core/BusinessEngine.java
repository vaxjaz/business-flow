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

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessEngine {

    private final BusinessDefRegistry registry;

    private final ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @BizLock()
    public <T, R> R process(String bizId,
                            BusinessType businessType,
                            BusinessAction action,
                            T request) {
        // 1. 构建上下文
        BusinessContext<T, R> ctx = BusinessContext.ofNew(bizId, businessType, action, request);
        // 2. 拿生命周期定义
        BusinessLifeCycleDefinition<T, R> def = registry.getDefinition(businessType);
        // 3. 拿到已经组装好的 Handler（内含所有步骤和回调）
        BusinessHandler<T, R> handler = def.getHandlerByAction(action);
        // 4. 获取guard，是否允许启动
        List<Guard<T, R>> actionGuard = def.getActionGuard(action);
        if (!CollectionUtils.isEmpty(actionGuard)) {
            try {
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
        // 5. wrap handler
        HandlerWrapper<T, R> wrap = wrap(handler);
        // 6. 执行 Handler，返回指定类型结果
        log.info("状态机启动 {}", ctx);
        R rawResult = wrap.execute(ctx);
        // 7. 将结果写回上下文
        ctx.setResp(rawResult);
        log.info("状态机完成 {}", ctx);
        // 8. 返回业务结果
        return rawResult;
    }

    private <T, R> HandlerWrapper<T, R> wrap(BusinessHandler<T, R> targetHandler) {
        HandlerInterceptors annotation = targetHandler.getClass().getAnnotation(HandlerInterceptors.class);
        List<BizInterceptor<T, R>> interceptors = new ArrayList<>();
        if (Objects.nonNull(annotation)) {
            for (Class<? extends BizInterceptor<T, R>> clzz : annotation.value()) {
                BizInterceptor<T, R> bean = applicationContext.getBean(clzz);
                interceptors.add(bean);
            }
        }
        interceptors = interceptors.stream().sorted(Comparator.comparing(BizInterceptor::order)).toList();
        return new HandlerWrapper<>(targetHandler, interceptors);
    }
}
