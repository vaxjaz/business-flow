package com.vaxjaz.business.flow.state_machine.config;

import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.definition.BusinessLifeCycleDefinition;
import com.vaxjaz.business.flow.state_machine.definition.BusinessLifeCycleDefinitionBuilder;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import com.vaxjaz.business.flow.state_machine.guard.TestGuard;
import com.vaxjaz.business.flow.state_machine.handler.one.ComplexHandler;
import com.vaxjaz.business.flow.state_machine.handler.one.DefaultHandler;
import com.vaxjaz.business.flow.state_machine.handler.one.GeneralHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BusinessFlowConfig {

    private final GeneralHandler generalHandler;

    private final DefaultHandler defaultHandler;

    private final ComplexHandler complexHandler;

    private final TestGuard guard;

    @Bean
    public BusinessLifeCycleDefinition eat() {
        return BusinessLifeCycleDefinitionBuilder.forType(BusinessType.EATING)
                .when(BusinessAction.INIT, generalHandler).guard(guard).next()
                .build();
    }


    @Bean
    public BusinessLifeCycleDefinition sleep() {
        return BusinessLifeCycleDefinitionBuilder.forType(BusinessType.SLEEPING)
                .when(BusinessAction.INIT, defaultHandler).guard(guard).next()
                .build();
    }

    @Bean
    public BusinessLifeCycleDefinition study() {
        return BusinessLifeCycleDefinitionBuilder.forType(BusinessType.STUDY)
                .when(BusinessAction.INIT, complexHandler).guard(guard).next()
                .build();
    }

}
