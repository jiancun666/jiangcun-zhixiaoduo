package com.semple.zhixiaoduo.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Excel 导入任务记录。
 * <p>数据库只保存任务级结果，具体失败数据由业务模块返回并写入失败明细 Excel。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("import_record")
public class ImportRecord extends BaseEntity {

    /**
     * 导入任务主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 当前任务所属企业。
     */
    private Long enterpriseId;

    /**
     * 模块开发人员定义的导入类型唯一编码。
     */
    private String importType;

    /**
     * 导入类型中文名称。
     */
    private String importTypeName;

    /**
     * 用户上传时的原始文件名。
     */
    private String importFileName;

    /**
     * 用户提交的本地上传地址或 HTTP、HTTPS 远程文件地址。
     */
    private String sourceFileUrl;

    /**
     * 对应导入模块的前端业务参数 JSON 快照。
     */
    private String requestParams;

    /**
     * 当前导入模块对应的可选功能权限编码，未接入权限时为空。
     */
    private String permissionCode;

    /**
     * 提交任务时是否为平台账号。
     */
    private Integer superUser;

    /**
     * 提交客户端类型：1 PC端，2移动端。
     */
    private Integer clientType;

    /**
     * 导入状态，取值见 ImportStatusEnum。
     */
    private Integer status;

    /**
     * Excel 有效数据总数。
     */
    private Integer totalCount;

    /**
     * 业务处理成功数量。
     */
    private Integer successCount;

    /**
     * 业务处理失败数量。
     */
    private Integer failureCount;

    /**
     * 导入失败明细TOS完整访问URL。
     */
    private String failureFileUrl;

    /**
     * 失败明细文件状态，取值见 FailureFileStatusEnum。
     */
    private Integer failureFileStatus;

    /**
     * 任务级异常说明，不保存逐行失败原因。
     */
    private String taskMessage;

    /**
     * 当前执行实例标识，便于排查多实例任务。
     */
    private String workerId;

    /**
     * 任务心跳时间，用于识别服务异常退出后的中断任务。
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

}
