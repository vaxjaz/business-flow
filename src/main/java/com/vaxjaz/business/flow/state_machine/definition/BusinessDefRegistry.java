package com.vaxjaz.business.flow.state_machine.definition;

import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 业务生命周期状态机注册器
 */
@Slf4j
@Component
public class BusinessDefRegistry implements InitializingBean {

    // 类型 → 生命周期定义
    private final Map<BusinessType, BusinessLifeCycleDefinition<?, ?>> lifecycleDefs = new EnumMap<>(BusinessType.class);


    @Autowired
    public BusinessDefRegistry(List<BusinessLifeCycleDefinition<?, ?>> defs) {
        // 注册所有生命周期定义
        for (BusinessLifeCycleDefinition<?, ?> def : defs) {
            BusinessType businessType = def.businessType();
            if (lifecycleDefs.containsKey(businessType)) {
                log.error("【{}】 状态机生命周期定义重复 【{}】", businessType, def);
            }
            lifecycleDefs.put(businessType, def);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public BusinessLifeCycleDefinition getDefinition(BusinessType type) {
        return Optional.ofNullable(lifecycleDefs.get(type))
                .orElseThrow(() -> new RuntimeException("Not found lifecycle for " + type));
    }

}
