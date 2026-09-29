package com.semple.zhixiaoduo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.semple.zhixiaoduo.bean.ChannelUser;
import com.semple.zhixiaoduo.model.bo.ChannelUserEmployeeNoBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserPaymentCardBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserPolicyBatchBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserPolicyScopeBO;
import com.semple.zhixiaoduo.model.bo.ChannelUserSaveBO;
import com.semple.zhixiaoduo.model.vo.ChannelUserCreatedVO;
import com.semple.zhixiaoduo.model.vo.PolicyBatchResultVO;
import com.semple.zhixiaoduo.model.vo.PolicyCountVO;

/**
 * 人员主业务接口。
 *
 * @author zengzhewen
 */
public interface ChannelUserService extends IService<ChannelUser> {
    /**
     * 新增人员。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    ChannelUserCreatedVO createUser(ChannelUserSaveBO request);
    /**
     * 编辑已发车人员。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    void updateUser(Long id, ChannelUserSaveBO request);
    /**
     * 统计政策范围内人员数。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    PolicyCountVO countPolicyUsers(ChannelUserPolicyScopeBO request);
    /**
     * 批量修改人员政策。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    PolicyBatchResultVO batchUpdatePolicy(ChannelUserPolicyBatchBO request);
    /**
     * 补录收款卡。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    void supplementPaymentCard(Long id, ChannelUserPaymentCardBO request);
    /**
     * 补录员工编号。
     *
     * @param id 业务记录 ID。
     * @param request 请求参数。
     */
    void supplementEmployeeNo(Long id, ChannelUserEmployeeNoBO request);
}
