package com.vaxjaz.business.flow.state_machine.guard;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;
import com.vaxjaz.business.flow.state_machine.bo.BusinessOneInputBo;
import com.vaxjaz.business.flow.state_machine.bo.BusinessOneOutputBo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestGuard implements Guard<BusinessOneInputBo, BusinessOneOutputBo> {

    @Override
    public boolean canProceed(BusinessContext<BusinessOneInputBo, BusinessOneOutputBo> ctx) {
        return true;
    }
}
