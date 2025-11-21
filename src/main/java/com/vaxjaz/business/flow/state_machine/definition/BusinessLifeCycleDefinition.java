package com.vaxjaz.business.flow.state_machine.definition;


import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import com.vaxjaz.business.flow.state_machine.guard.Guard;
import com.vaxjaz.business.flow.state_machine.handler.BusinessHandler;

import java.util.List;


public interface BusinessLifeCycleDefinition<T, R> {

    /**
     *
     * @return
     */
    BusinessType businessType();


    /**
     * 某个工单类型生命周期中action对应的业务handler
     *
     * @param actionEnums
     * @return
     */
    BusinessHandler<T, R> getHandlerByAction(BusinessAction actionEnums);


    /**
     * 状态守卫，一些默认简单判断当前ctx是否允许启动执行WorkOrderHandler
     * @param actionEnums
     * @return
     */
    List<Guard<T, R>> getActionGuard(BusinessAction actionEnums);

}
