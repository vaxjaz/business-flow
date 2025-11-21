package com.vaxjaz.business.flow.state_machine.handler;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;

public interface BusinessHandler<T, R> {

    /**
     * 必须实现
     *
     * @param req
     * @return
     */
    R execute(BusinessContext<T, R> req);

    /**
     * entry hook
     *
     * @param context
     * @param result
     */
    default void onEnter(BusinessContext<T, R> context, R result) {

    }

    /**
     * complete hook
     *
     * @param context
     * @param result
     */
    default void onCompleted(BusinessContext<T, R> context, R result) {

    }

}
