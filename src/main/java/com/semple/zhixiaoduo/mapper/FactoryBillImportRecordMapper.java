package com.semple.zhixiaoduo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.semple.zhixiaoduo.bean.FactoryBillImportRecord;
import org.apache.ibatis.annotations.Param;

/**
 * 厂家账单导入记录数据访问接口。
 *
 * @author zengzhewen
 */
public interface FactoryBillImportRecordMapper extends BaseMapper<FactoryBillImportRecord> {

    /**
     * 按企业和记录主键物理删除厂家账单导入记录。
     *
     * @param enterpriseId 企业 ID
     * @param id 导入记录 ID
     * @return 删除记录数
     */
    int deletePhysically(@Param("enterpriseId") Long enterpriseId, @Param("id") Long id);
}
