package com.vaxjaz.business.flow.state_machine.definition;


import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import com.vaxjaz.business.flow.state_machine.guard.Guard;
import com.vaxjaz.business.flow.state_machine.handler.BusinessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.*;

/**
 * 工单生命周期定义builder
 */
@Slf4j
public class BusinessLifeCycleDefinitionBuilder {


    private final BusinessType workType;
    private final Map<BusinessAction, BusinessHandler> handlerMap = new EnumMap<>(BusinessAction.class);
    private final Map<BusinessAction, List<Guard>> guardMap = new EnumMap<>(BusinessAction.class);

    private BusinessLifeCycleDefinitionBuilder(BusinessType workType) {
        this.workType = workType;
    }

    public static BusinessLifeCycleDefinitionBuilder forType(BusinessType type) {
        return new BusinessLifeCycleDefinitionBuilder(type);
    }

    public BusinessLifeCycleDefinition build() {

        return new BusinessLifeCycleDefinition() {
            @Override
            public BusinessType businessType() {
                return workType;
            }

            @Override
            public BusinessHandler getHandlerByAction(BusinessAction actionEnums) {
                return Optional.ofNullable(handlerMap.get(actionEnums)).orElseThrow(() -> new IllegalArgumentException(String.format("action %s 没有对应的业务handler", actionEnums)));
            }

            @Override
            public List<Guard> getActionGuard(BusinessAction actionEnums) {
                return guardMap.get(actionEnums);
            }
        };
    }


    /**
     * 定义动作（action）与处理器（handler）的关系。
     * 使用 `when` 表示在特定的动作发生时会执行相应的处理器。
     */
    public StepBuilder when(BusinessAction action, BusinessHandler handler) {
        if (handlerMap.containsKey(action)) {
            log.warn("工单 【{}】，状态机定义 action 【{}】 重复", this.workType, action);
        }
        handlerMap.put(action, handler);
        return new StepBuilder(this, action);
    }


    public static class StepBuilder {

        private final BusinessLifeCycleDefinitionBuilder parent;
        private final BusinessAction action;

        public StepBuilder(BusinessLifeCycleDefinitionBuilder parent, BusinessAction action) {
            this.parent = parent;
            this.action = action;
        }

        /**
         * 状态guard
         * @param guard
         * @return
         */
        public StepBuilder guard(Guard... guard) {
            Assert.notNull(guard, "guard 不能为空！");
            parent.guardMap.put(action, Arrays.asList(guard));
            return this;
        }


    }


}
