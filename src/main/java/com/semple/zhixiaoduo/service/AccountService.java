package com.semple.zhixiaoduo.service;

import com.semple.zhixiaoduo.model.bo.AccountCreateRequest;
import com.semple.zhixiaoduo.model.bo.AccountOptionRequest;
import com.semple.zhixiaoduo.model.bo.AccountPageRequest;
import com.semple.zhixiaoduo.model.bo.AccountUpdateRequest;
import com.semple.zhixiaoduo.model.bo.ChangePasswordRequest;
import com.semple.zhixiaoduo.model.vo.AccountDetailResponse;
import com.semple.zhixiaoduo.model.vo.AccountListResponse;
import com.semple.zhixiaoduo.model.vo.AccountOptionVO;
import com.semple.zhixiaoduo.utils.ResultPage;

import java.util.List;

/**
 * 账号管理服务。
 */
public interface AccountService {

    /**
     * 新增当前企业的企业账号。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    Long create(AccountCreateRequest request);

    /**
     * 查询当前登录账号详情。
     *
     * @return 账号详情
     */
    AccountDetailResponse detail();

    /**
     * 仅修改当前企业账号姓名。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    void updateName(Long id, AccountUpdateRequest request);

    /**
     * 逻辑删除待激活账号。
     *
     * @param id 业务记录 ID。
     */
    void delete(Long id);

    /**
     * 启用已停用账号。
     *
     * @param id 业务记录 ID。
     */
    void enable(Long id);

    /**
     * 停用启用中的账号。
     *
     * @param id 业务记录 ID。
     */
    void disable(Long id);

    /**
     * 将企业账号密码重置为系统默认密码。
     *
     * @param id 业务记录 ID。
     */
    void resetPassword(Long id);

    /**
     * 当前登录账号校验旧密码后修改自己的密码。
     *
     * @param request 请求参数。
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * 查询当前企业账号列表，平台账号不在列表展示。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    ResultPage<AccountListResponse> page(AccountPageRequest request);

    /**
     * 查询当前企业账号下拉选项。
     *
     * @param request 筛选参数，不包含分页字段
     * @return 账号 ID 和账号所属人姓名
     */
    List<AccountOptionVO> listOptions(AccountOptionRequest request);
}
