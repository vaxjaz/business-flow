package com.vaxjaz.business.flow.state_machine.bo;

import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessContext<T, R> {

    private String businessId;

    private BusinessType businessType;

    private BusinessAction action;

    private T input;

    private R resp;

    public BusinessContext(String businessId, BusinessType businessType, BusinessAction action, T input) {
        this.businessId = businessId;
        this.businessType = businessType;
        this.action = action;
        this.input = input;
    }

    public static <T, R> BusinessContext<T, R> ofNew(String bizId, BusinessType businessType, BusinessAction action, T request) {
        return new BusinessContext<>(bizId, businessType, action, request);
    }
}
