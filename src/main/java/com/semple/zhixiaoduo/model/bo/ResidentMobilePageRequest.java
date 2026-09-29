package com.semple.zhixiaoduo.model.bo;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 移动端驻场人员分页请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResidentMobilePageRequest extends PageRequest {

    /**
     * 移动端单页最大条数，避免客户端请求过大的数据集。
     */
    public static final int MAX_PAGE_SIZE = 15;

    /**
     * 页码，从 1 开始。
     *
     * @return 处理结果。
     */
    @NotNull(message = "页码不能为空")
    @Positive(message = "页码必须为正数")
    @Override
    public Integer getPageIndex() {
        return super.getPageIndex();
    }

    /**
     * 每页数量。
     *
     * @return 处理结果。
     */
    @NotNull(message = "每页数量不能为空")
    @Positive(message = "每页数量必须为正数")
    @Override
    public Integer getPageSize() {
        return super.getPageSize();
    }

    /**
     * 移动端按驻场人员姓名模糊查询。
     */
    @Size(max = 64, message = "搜索关键词长度不能超过64个字符")
    private String keyword;

}
