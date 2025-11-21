package com.vaxjaz.business.flow.state_machine.bo;

import com.vaxjaz.business.flow.state_machine.action.BusinessAction;
import com.vaxjaz.business.flow.state_machine.enums.BusinessType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务上下文 - 贯穿整个业务流程的核心数据载体
 *
 * 该类封装了业务处理过程中需要的所有关键信息，包括：
 * - 业务标识信息（businessId, businessType）
 * - 业务动作（action）
 * - 输入参数（input）
 * - 输出结果（resp）
 *
 * 业务上下文在整个处理流程中传递，守卫、拦截器、处理器都可以访问和修改上下文信息。
 *
 * @param <T> 输入参数类型
 * @param <R> 返回结果类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessContext<T, R> {

    /** 业务唯一标识，用于追踪和定位具体的业务实例 */
    private String businessId;

    /** 业务类型，决定使用哪个生命周期定义 */
    private BusinessType businessType;

    /** 业务动作，决定执行哪个处理器 */
    private BusinessAction action;

    /** 输入参数，业务处理的输入数据 */
    private T input;

    /** 响应结果，业务处理的输出数据 */
    private R resp;

    /**
     * 构造业务上下文（不包含响应结果）
     *
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param action 业务动作
     * @param input 输入参数
     */
    public BusinessContext(String businessId, BusinessType businessType, BusinessAction action, T input) {
        this.businessId = businessId;
        this.businessType = businessType;
        this.action = action;
        this.input = input;
    }

    /**
     * 创建新的业务上下文实例（工厂方法）
     *
     * 这是创建业务上下文的推荐方式，提供了更清晰的语义。
     *
     * @param bizId 业务唯一标识
     * @param businessType 业务类型
     * @param action 业务动作
     * @param request 请求参数
     * @param <T> 输入参数类型
     * @param <R> 返回结果类型
     * @return 新创建的业务上下文实例
     */
    public static <T, R> BusinessContext<T, R> ofNew(String bizId, BusinessType businessType, BusinessAction action, T request) {
        return new BusinessContext<>(bizId, businessType, action, request);
    }
}
