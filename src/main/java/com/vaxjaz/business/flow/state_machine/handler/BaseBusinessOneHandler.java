package com.vaxjaz.business.flow.state_machine.handler;

import com.vaxjaz.business.flow.state_machine.bo.BusinessContext;
import com.vaxjaz.business.flow.state_machine.bo.BusinessOneInputBo;
import com.vaxjaz.business.flow.state_machine.bo.BusinessOneOutputBo;

public abstract class BaseBusinessOneHandler implements BusinessHandler<BusinessOneInputBo, BusinessOneOutputBo> {

    @Override
    public void onEnter(BusinessContext<BusinessOneInputBo, BusinessOneOutputBo> context, BusinessOneOutputBo result) {
        BusinessHandler.super.onEnter(context, result);
    }

    @Override
    public void onCompleted(BusinessContext<BusinessOneInputBo, BusinessOneOutputBo> context, BusinessOneOutputBo result) {
        BusinessHandler.super.onCompleted(context, result);
    }

    @Override
    public BusinessOneOutputBo execute(BusinessContext<BusinessOneInputBo, BusinessOneOutputBo> req) {
        return bizOneProcess(req);
    }

    protected BusinessOneOutputBo bizOneProcess(BusinessContext<BusinessOneInputBo, BusinessOneOutputBo> context) {
        BusinessOneOutputBo resp = new BusinessOneOutputBo();
        BusinessOneInputBo input = context.getInput();
        bizStepOne(input);
        bizStepTwo(input);
        bizStepThree(input);
        return resp;
    }

    protected void bizStepThree(BusinessOneInputBo input) {

    }

    protected void bizStepTwo(BusinessOneInputBo input) {

    }

    protected void bizStepOne(BusinessOneInputBo input) {

    }

}
