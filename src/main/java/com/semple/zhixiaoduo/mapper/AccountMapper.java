package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.Account;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 账号数据访问层。
 */
public interface AccountMapper extends BaseMapper<Account> {

    /**
     * 查询创建人名称，包含已逻辑删除账号，保证历史审计信息仍可展示。
     *
     * @param ids 业务记录 ID 集合。
     * @return 处理结果。
     */
    // 审计回显只按已过滤结果中的创建人 ID 查询，避免账号数据权限再次裁剪创建人姓名。
    @InterceptorIgnore(dataPermission = "true")
    List<Account> selectCreatorNames(@Param("ids") List<Long> ids);

    /**
     * 按企业查询创建人名称，包含已逻辑删除账号，避免跨企业回显账号信息。
     *
     * @param enterpriseId 企业 ID
     * @param ids 账号 ID 列表
     * @return 创建人账号列表
     * @author zengzhewen
     */
    // 企业条件和账号 ID 均由业务端明确传入，该辅助查询不参与主业务数据权限计算。
    @InterceptorIgnore(dataPermission = "true")
    List<Account> selectEnterpriseCreatorNames(@Param("enterpriseId") Long enterpriseId,
                                               @Param("ids") List<Long> ids);
}
