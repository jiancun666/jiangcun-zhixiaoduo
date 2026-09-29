package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.semple.zhixiaoduo.annotation.DataPermissionTarget;
import com.semple.zhixiaoduo.bean.Account;
import com.semple.zhixiaoduo.bean.Enterprise;
import com.semple.zhixiaoduo.bean.Factory;
import com.semple.zhixiaoduo.bean.ResidentFactory;
import com.semple.zhixiaoduo.model.vo.ResidentMobilePageVO;
import com.semple.zhixiaoduo.model.vo.ResidentPageVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 驻场人员数据访问接口。
 */
public interface ResidentMapper extends BaseMapper<ResidentFactory> {

    /**
     * 按账号锁定当前企业下的全部驻场记录，新增时用于判断账号是否已经绑定工厂。
     *
     * @param enterpriseId 当前企业 ID
     * @param accountId 关联账号 ID
     * @return 当前企业下该账号的驻场记录，正常记录优先返回
     */
    List<ResidentFactory> selectResidentsByAccountForUpdate(@Param("enterpriseId") Long enterpriseId,
                                                             @Param("accountId") Long accountId);

    /**
     * 锁定同企业、同工厂下其他驻场记录的重复身份证号。
     *
     * @param enterpriseId 当前企业 ID
     * @param residentId 当前编辑的驻场记录 ID；新增时传 null
     * @param factoryId 工厂 ID
     * @param idCard 身份证号
     * @return 其他有效重复记录；不存在时返回 null
     */
    ResidentFactory selectOtherResidentByFactoryAndIdCardForUpdate(
            @Param("enterpriseId") Long enterpriseId,
            @Param("residentId") Long residentId,
            @Param("factoryId") Long factoryId,
            @Param("idCard") String idCard);

    /**
     * 锁定同企业、同账号、同工厂下其他有效驻场记录。
     *
     * @param enterpriseId 当前企业 ID
     * @param residentId 当前编辑的驻场记录 ID；新增时传 null
     * @param accountId 关联账号 ID
     * @param factoryId 工厂 ID
     * @return 其他有效重复记录；不存在时返回 null
     */
    ResidentFactory selectOtherResidentByAccountAndFactoryForUpdate(
            @Param("enterpriseId") Long enterpriseId,
            @Param("residentId") Long residentId,
            @Param("accountId") Long accountId,
            @Param("factoryId") Long factoryId);

    /**
     * 按驻场表主键锁定当前企业下的有效驻场记录。
     *
     * @param residentId 驻场记录 ID
     * @param enterpriseId 当前企业 ID
     * @return 有效驻场记录；不存在时返回 null
     */
    ResidentFactory selectResidentForUpdate(@Param("residentId") Long residentId,
                                             @Param("enterpriseId") Long enterpriseId);

    /**
     * 锁定企业账号，保证同一个账号不会并发建立多条驻场记录。
     *
     * @param accountId 账号 ID
     * @param enterpriseId 当前企业 ID
     * @return 当前企业账号；不存在时返回 null
     */
    Account selectEnterpriseAccountForUpdate(@Param("accountId") Long accountId,
                                             @Param("enterpriseId") Long enterpriseId);

    /**
     * 锁定当前登录的账号，防止写事务执行期间账号状态或类型变化。
     *
     * @param accountId 账号 ID
     * @return 启用中的平台或企业账号；不满足条件时返回 null
     */
    Account selectAccountForShare(@Param("accountId") Long accountId);

    /**
     * 锁定有效企业，防止写事务执行期间企业被删除。
     *
     * @param enterpriseId 企业 ID
     * @return 有效企业；不存在时返回 null
     */
    Enterprise selectEnterpriseForShare(@Param("enterpriseId") Long enterpriseId);

    /**
     * 恢复已逻辑删除的驻场记录，并重置本次业务所需字段。
     *
     * @param resident 驻场记录
     * @return 受影响行数
     */
    int restoreDeleted(ResidentFactory resident);

    /**
     * 分页查询驻场人员。
     *
     * @param page 分页参数
     * @param enterpriseId 企业 ID
     * @return 驻场人员分页
     */
    @DataPermissionTarget(alias = "rf")
    Page<ResidentPageVO> selectResidentPage(Page<ResidentPageVO> page,
                                            @Param("enterpriseId") Long enterpriseId);

    /**
     * 分页查询移动端驻场人员，仅返回姓名、手机号和工厂名称。
     *
     * @param page 分页参数
     * @param enterpriseId 企业 ID
     * @param keyword 姓名搜索关键词
     * @return 移动端驻场人员分页
     */
    @DataPermissionTarget(alias = "rf")
    Page<ResidentMobilePageVO> selectMobileResidentPage(Page<ResidentMobilePageVO> page,
                                                        @Param("enterpriseId") Long enterpriseId,
                                                        @Param("keyword") String keyword);

    /**
     * 锁定指定企业下未删除的工厂记录。
     *
     * @param enterpriseId 企业 ID
     * @param factoryId 工厂 ID
     * @return 锁定后的工厂记录；不存在时返回 null
     */
    Factory selectFactoryForUpdate(@Param("enterpriseId") Long enterpriseId,
                                   @Param("factoryId") Long factoryId);
}
