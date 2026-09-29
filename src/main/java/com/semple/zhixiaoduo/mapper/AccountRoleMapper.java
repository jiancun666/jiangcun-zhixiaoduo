package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.bean.AccountRole;
import com.semple.zhixiaoduo.model.vo.RoleAccountListResponse;
import com.semple.zhixiaoduo.model.vo.RoleAvailableAccountResponse;
import org.apache.ibatis.annotations.Param;

/**
 * 账号角色关系数据访问接口。
 */
public interface AccountRoleMapper extends BaseMapper<AccountRole> {

    /**
     * 分页查询指定角色已经关联的账号。
     *
     * @param page 分页参数
     * @param enterpriseId 当前企业ID
     * @param roleId 角色ID
     * @param accountName 账号或姓名搜索条件
     * @return 角色成员分页
     */
    Page<RoleAccountListResponse> selectRoleAccountPage(Page<RoleAccountListResponse> page,
                                                        @Param("enterpriseId") Long enterpriseId,
                                                        @Param("roleId") Long roleId,
                                                        @Param("accountName") String accountName);

    /**
     * 分页查询指定角色尚未关联的企业账号。
     *
     * @param page 分页参数
     * @param enterpriseId 当前企业ID
     * @param roleId 角色ID
     * @param accountName 账号或姓名搜索条件
     * @return 可添加账号分页
     */
    Page<RoleAvailableAccountResponse> selectAvailableAccountPage(
            Page<RoleAvailableAccountResponse> page,
            @Param("enterpriseId") Long enterpriseId,
            @Param("roleId") Long roleId,
            @Param("accountName") String accountName);
}
