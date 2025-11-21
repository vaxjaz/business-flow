package com.vaxjaz.business.flow.state_machine.interceptor;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;

public interface BizInterceptor<T, R> {

    void beforeTransition(BusinessContext<T, R> request);

    void afterTransition(BusinessContext<T, R> request, R result);

    default int order() {
        return -1;
    }

}
