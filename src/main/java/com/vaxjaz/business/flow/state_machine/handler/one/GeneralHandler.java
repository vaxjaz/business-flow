package com.vaxjaz.business.flow.state_machine.handler.one;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;
import com.vaxjaz.business.flow.state_machine.bo.BusinessOneInputBo;
import com.vaxjaz.business.flow.state_machine.bo.BusinessOneOutputBo;
import com.vaxjaz.business.flow.state_machine.handler.BaseBusinessOneHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralHandler extends BaseBusinessOneHandler {

    @Override
    protected BusinessOneOutputBo bizOneProcess(BusinessContext<BusinessOneInputBo, BusinessOneOutputBo> context) {
        BusinessOneOutputBo outputBo = super.bizOneProcess(context);
        specialOneLogic(context.getInput(), outputBo);
        return outputBo;
    }

    private void specialOneLogic(BusinessOneInputBo input, BusinessOneOutputBo outputBo) {

    }

}
