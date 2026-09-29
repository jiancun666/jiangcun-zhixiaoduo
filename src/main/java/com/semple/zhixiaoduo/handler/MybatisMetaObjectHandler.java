package com.semple.zhixiaoduo.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.semple.zhixiaoduo.utils.UserKit;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author longfei
 * @classname MybatisMetaObjectHandler
 * @date 2024-12-03 11:20
 * @description mybatisplus自动填充字段
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    /**
     * 新增数据时自动填充创建时间、更新时间和当前登录用户。
     *
     * @param metaObject 实体元数据
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        this.strictInsertFill(metaObject, "createTime", Date.class, now);
        this.strictInsertFill(metaObject, "updateTime", Date.class, now);
        Long userId = UserKit.getUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    /**
     * 更新数据时自动填充更新时间。
     *
     * @param metaObject 实体元数据
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
        Long userId = UserKit.getUserId();
        if (userId != null) {
            this.setFieldValByName("updateBy", userId, metaObject);
        }
    }
}
