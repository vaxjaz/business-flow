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
 * 业务生命周期定义注册器
 *
 * 该类负责管理和查找所有业务类型的生命周期定义。
 * 在 Spring 容器启动时，会自动收集所有的 BusinessLifeCycleDefinition Bean，
 * 并按照业务类型进行注册，以便业务引擎能够快速查找。
 *
 * 主要职责：
 * 1. 自动注册所有生命周期定义
 * 2. 检测重复定义并报警
 * 3. 提供快速查找服务
 * 4. 保证定义的唯一性
 */
@Slf4j
@Component
public class BusinessDefRegistry implements InitializingBean {

    /** 业务类型到生命周期定义的映射表 */
    private final Map<BusinessType, BusinessLifeCycleDefinition<?, ?>> lifecycleDefs = new EnumMap<>(BusinessType.class);


    /**
     * 构造注册器，自动注册所有生命周期定义
     *
     * Spring 会自动注入容器中所有的 BusinessLifeCycleDefinition Bean
     *
     * @param defs 所有生命周期定义的列表
     */
    @Autowired
    public BusinessDefRegistry(List<BusinessLifeCycleDefinition<?, ?>> defs) {
        // 遍历所有定义并注册到映射表中
        for (BusinessLifeCycleDefinition<?, ?> def : defs) {
            BusinessType businessType = def.businessType();

            // 检测重复定义，如果存在相同业务类型的定义则记录错误日志
            if (lifecycleDefs.containsKey(businessType)) {
                log.error("【{}】 状态机生命周期定义重复 【{}】", businessType, def);
            }

            lifecycleDefs.put(businessType, def);
        }

        log.info("业务生命周期定义注册完成，共注册 {} 个业务类型", lifecycleDefs.size());
    }

    /**
     * Spring Bean 初始化完成后的回调方法
     *
     * 可以在此方法中执行一些初始化后的校验或日志记录
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 预留扩展点，当前无需特殊处理
    }

    /**
     * 根据业务类型获取对应的生命周期定义
     *
     * @param type 业务类型
     * @return 对应的生命周期定义
     * @throws RuntimeException 如果未找到对应的生命周期定义
     */
    @SuppressWarnings("unchecked")
    public <T, R> BusinessLifeCycleDefinition<T, R> getDefinition(BusinessType type) {
        return (BusinessLifeCycleDefinition<T, R>) Optional.ofNullable(lifecycleDefs.get(type))
                .orElseThrow(() -> new RuntimeException("Not found lifecycle for " + type));
    }

}
