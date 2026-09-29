package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Excel 异步导出任务记录。
 * <p>数据库只保存任务信息和查询条件，不保存具体导出数据。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("export_record")
public class ExportRecord extends BaseEntity {

    /**
     * 导出任务主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 当前任务所属企业，系统管理范围使用 0。
     */
    private Long enterpriseId;

    /**
     * 模块开发人员定义的导出类型唯一编码。
     */
    private String exportType;

    /**
     * 导出内容中文名称。
     */
    private String exportContent;

    /**
     * 面向用户展示和下载的文件名。
     */
    private String fileName;

    /**
     * TOS文件完整可访问URL。
     */
    private String fileUrl;

    /**
     * 导出条件 JSON 快照，用于异步执行和中断恢复。
     */
    private String requestParams;

    /**
     * 当前导出模块对应的可选功能权限编码，未接入权限时为空。
     */
    private String permissionCode;

    /**
     * 提交任务时的数据权限快照 JSON，未接入权限时为空。
     */
    private String dataPermissionSnapshot;

    /**
     * 导出状态，取值见 ExportStatusEnum。
     */
    private Integer status;

    /**
     * 已经写入 Excel 的数据数量。
     */
    private Integer exportedCount;

    /**
     * 最终文件大小，单位字节。
     */
    private Long fileSize;

    /**
     * 任务级异常说明。
     */
    private String taskMessage;

    /**
     * 当前执行标识，用于防止恢复后的旧线程继续更新任务。
     */
    private String workerId;

    /**
     * 最近一次分页写入完成时间。
     */
    private Date heartbeatTime;

    /**
     * 任务首次开始执行时间。
     */
    private Date startTime;

    /**
     * 任务结束时间。
     */
    private Date endTime;

    /**
     * 服务异常退出后的重新执行次数。
     */
    private Integer resumeCount;

    /**
     * 提交任务时是否为平台账号。
     */
    private Integer superUser;

    /**
     * 提交客户端类型：1 PC端，2移动端。
     */
    private Integer clientType;
}
