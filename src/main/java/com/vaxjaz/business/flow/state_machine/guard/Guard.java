package com.vaxjaz.business.flow.state_machine.guard;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;

/**
 * handler守卫
 */
public interface Guard<T, R> {

    boolean canProceed(BusinessContext<T, R> ctx);

}

