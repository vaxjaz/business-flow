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
public class ComplexHandler extends BaseBusinessOneHandler {

    @Override
    protected BusinessOneOutputBo bizOneProcess(BusinessContext<BusinessOneInputBo, BusinessOneOutputBo> context) {
        BusinessOneInputBo input = context.getInput();
        if (condition(input)) {
            return super.bizOneProcess(context);
        }
        BusinessOneOutputBo outputBo = new BusinessOneOutputBo();
        super.bizStepOne(input);
        super.bizStepTwo(input);
        bizStepThree(input);
        specialComplex(input);
        return outputBo;
    }

    private void specialComplex(BusinessOneInputBo input) {

    }

    @Override
    protected void bizStepThree(BusinessOneInputBo input) {
        // self override
    }


    private boolean condition(BusinessOneInputBo input) {
        return true;
    }

}
