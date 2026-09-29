package com.semple.zhixiaoduo.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName ExceptionEnum
 * @Description 异常定义
 * @Author aofaming
 * @Date 2023/12/4 18:10
 * @Version 1.0
 */
@Getter
@AllArgsConstructor
public enum ExceptionEnum {
    SYSTEM_ERROR("111111","系统异常"),
    AD_TOKEN_EXPIRE("100001","用户ADtoken已失效，请重新授权！"),
    PARAM_ERROR("100002","参数有误！"),
    TRANPIC_ERROR("100003","转换成图片异常！"),
    DELETE_ERROR("100004","删除失败，请稍后再试"),
    UPDATE_ERROR("100005","更新失败，请稍后再试"),
    INSERT_ERROR("100006","添加失败，请稍后再试"),
    FILE_SUFFIX_ERROR("100008","文件后缀不符合"),
    EXCEL_TITLE_ERROR("100009","EXCEL表头不符合"),
    FILE_EMPTY_ERROR("100010","文件为空"),
    FILE_UPLOAD_ERROR("100011","文件上传失败"),
    FILE_NOT_EXISTS("100012","文件不存在"),
    FILE_STORAGE_MODE_ERROR("100013", "文件存储模式不合法"),
    DUPLICATE_PARAM("100014","参数存在重复值！"),
    OVERRUNNUM_PARAM("100015","参数个数超过最大值！"),
    TOS_UPLOAD_TASK_NOT_EXISTS("100016", "TOS上传任务不存在"),
    TOS_UPLOAD_DISABLED("100017", "TOS文件上传未启用"),
    TOS_UPLOAD_CONFIG_ERROR("100018", "TOS文件上传配置不完整"),

    RECORD_NO_FOUNT("200001","记录不存在"),
    RECORD_FOUNT("200002","记录已存在"),

    LOGIN_ERROR("300001","登录失败，请联系技术人员"),
    NOT_LOGIN("300002","用户未登录, 请重新登录"),
    CAPTCHA_ERROR("300003", "验证码错误或已失效"),
    ACCOUNT_PASSWORD_ERROR("300004", "账号或密码错误"),
    ACCOUNT_DISABLED("300005", "账号已停用"),
    LOGIN_SESSION_EXPIRE("300006", "登录状态已失效，请重新登录"),
    ACCOUNT_STATUS_ERROR("300007", "当前账号状态不允许执行该操作"),
    OLD_PASSWORD_ERROR("300008", "旧密码不正确"),
    ACCOUNT_DUPLICATE("300009", "当前企业已存在相同账号"),
    PLATFORM_ACCOUNT_OPERATION_ERROR("300010", "平台账号不允许执行该操作"),
    ENTERPRISE_DUPLICATE("300011", "企业名称或企业简称已存在"),
    ENTERPRISE_NOT_EXISTS("300012", "企业不存在"),
    REDIS_OPERATION_ERROR("300013", "缓存服务异常，请稍后重试"),

    USER_NOT_EXISTS("400001","用户不存在" ),

    TOKEN_EXPIRE("500001","用户token已失效，请重新授权！"),
    REFRESH_TOKEN_ERROR("500002", "登录状态已失效，请重新登录"),
    REFRESH_TOKEN_EXPIRE("500003", "登录状态已过期，请重新登录"),


    PLATFORM_USER_TOKEN_EXPIRE("600002","用户token已失效"),

    OCEAN_ACCESS_TOKEN_NOT_EXIST("700001","巨量accessToken不存在"),
    OCEAN_ACCESS_TOKEN_EXPIRE("700002","巨量accessToken已过期"),


    AUTH_FAIL("800001","无权限访问"),
    ADMIN_ROLE_UPDATE_ERROR("800002","超级管理员不允许更新操作"),
    USER_ROLE_UPDATE_ERROR("800003","用户已存在相同角色"),
    DEPT_ROLE_UPDATE_ERROR("800004","部门已存在相同角色"),
    ADMIN_ROLE_DELETE_ERROR("800002","超级管理员不允许删除操作"),
    ROLE_NOTEXISTIS_ERROR("800003","角色已删除"),

    PERMISSION_CONFIG_ERROR("800007", "权限配置不完整"),
    ROLE_NAME_EXISTS("800008", "当前企业已存在相同角色名称"),
    BUILT_IN_ROLE_NAME_ERROR("800009", "内置角色名称不允许修改"),
    BUILT_IN_ROLE_DELETE_ERROR("800010", "内置角色不允许删除"),
    ROLE_PERMISSION_SCOPE_ERROR("800011", "角色数据权限配置不合法"),
    ACCOUNT_ROLE_ERROR("800012", "账号角色配置不合法"),

    MENU_PARENT_NOT_EXIST("800005", "父级菜单不存在"),
    MENU_PARENT_TYPE_ERROR("800006", "父级菜单类型必须为 CATALOG 或 MENU"),

    FACTORY_NAME_EXISTS("900001", "工厂名称已存在"),
    FACTORY_NOT_EXISTS("900002", "工厂不存在"),
    AREA_NOT_EXISTS("900003", "行政区划不存在"),
    FACTORY_LOCATION_LEVEL_ERROR("900004", "工厂所在地必须选择到第4级"),
    FACTORY_STATUS_ERROR("900005", "工厂状态非法"),
    FACTORY_BILL_REPLACE_ERROR("910006", "厂家账单替换失败"),
    CHANNEL_NAME_EXISTS("910001", "渠道名称已存在"),
    CHANNEL_NOT_EXISTS("910002", "渠道不存在"),
    CHANNEL_MEMBER_NOT_EXISTS("910003", "渠道成员账号不存在或不属于当前企业"),
    CHANNEL_MEMBER_ALREADY_BOUND("910004", "渠道成员已属于其他渠道"),
    CHANNEL_BOUND_USER_DELETE_FORBIDDEN("910005", "该渠道已绑定人员，不允许删除"),
    CHANNEL_BOUND_BILL_DELETE_FORBIDDEN("910006", "该渠道已绑定渠道账单，不允许删除"),

    FACTORY_BILL_MONTH_ERROR("920001", "厂家账单月份格式必须为yyyy-MM"),
    FACTORY_BILL_FILE_PATH_ERROR("920002", "厂家账单文件路径非法"),
    FACTORY_BILL_EMPTY_ERROR("920003", "厂家账单文件没有数据"),
    FACTORY_BILL_FAILURE_FILE_ERROR("920004", "厂家账单失败文件生成失败"),
    FACTORY_BILL_IMPORT_CONFLICT("920005", "同一账单正在导入，请稍后重试"),

    CHANNEL_BILL_EXISTS("930001", "该渠道在当前结算月份已存在相同类型账单"),
    CHANNEL_BILL_NOT_EXISTS("930002", "渠道账单不存在"),
    CHANNEL_BILL_STATUS_ERROR("930003", "渠道账单不是待确认状态"),
    CHANNEL_BILL_MONTH_ERROR("930004", "渠道账单结算月份格式必须为yyyy-MM"),
    CHANNEL_BILL_CREATOR_ERROR("930005", "创建人账号不存在或不属于当前企业"),
    CHANNEL_BILL_SETTLED_REIMPORT_FORBIDDEN("930006", "该结算月份的渠道账单已结算，不允许再次添加"),
    CHANNEL_BILL_IMPORT_EMPTY_ERROR("930007", "渠道账单导入文件没有数据"),


    IMPORT_HANDLER_NOT_FOUND("940007", "未找到对应的导入处理器"),
    IMPORT_FILE_TYPE_ERROR("940008", "仅支持 xls 或 xlsx 格式文件"),
    IMPORT_RECORD_NOT_EXISTS("940009", "导入记录不存在"),
    IMPORT_TASK_STATUS_ERROR("940010", "当前导入任务状态不允许执行该操作"),
    IMPORT_FAILURE_FILE_NOT_READY("940011", "失败明细文件尚未生成"),
    IMPORT_SOURCE_FILE_CHANGED("940012", "导入源文件不存在或已被修改"),
    IMPORT_FILE_URL_ERROR("940013", "导入文件地址不合法"),
    IMPORT_FILE_DOWNLOAD_ERROR("940014", "导入文件下载失败"),
    IMPORT_FILE_SIZE_ERROR("940015", "导入文件大小超出限制"),
    IMPORT_ROWS_LIMIT_ERROR("940016", "导入数据量超出限制"),
    IMPORT_RESULT_ERROR("940017", "导入模块返回结果不正确"),
    IMPORT_PARAMS_ERROR("940018", "导入业务参数不正确"),

    EXPORT_HANDLER_NOT_FOUND("950001", "未找到对应的导出处理器"),
    EXPORT_RECORD_NOT_EXISTS("950002", "导出记录不存在"),
    EXPORT_FILE_NOT_READY("950003", "导出文件尚未生成"),
    EXPORT_TASK_STATUS_ERROR("950004", "当前导出任务状态不允许执行该操作"),
    EXPORT_FILE_ERROR("950005", "导出文件生成失败"),

    CHANNEL_USER_NOT_EXISTS("960001", "人员不存在"),
    CHANNEL_USER_STATUS_ERROR("960002", "人员状态非法"),
    CHANNEL_USER_ID_CARD_ERROR("960003", "身份证号格式非法"),
    CHANNEL_USER_MOBILE_ERROR("960004", "手机号格式非法"),
    CHANNEL_USER_POLICY_INCOMPLETE("960005", "人员政策信息不完整"),
    CHANNEL_USER_ID_CARD_DUPLICATE("960006", "同工厂存在重复身份证人员"),
    CHANNEL_USER_OCR_NOT_CONNECTED("960007", "OCR 服务暂未接入"),
    CHANNEL_USER_OCR_CACHE_EXPIRED("960008", "OCR 识别结果已失效，请重新识别"),
    CHANNEL_USER_EMPLOYEE_NO_EXISTS("960009", "当前工厂已存在相同员工编号"),
    CHANNEL_USER_IMPORT_FILE_PATH_ERROR("960010", "人员导入文件路径非法"),
    CHANNEL_USER_IMPORT_HEADER_ERROR("960011", "人员导入文件表头不符合"),
    CHANNEL_USER_IMPORT_FAILURE_FILE_ERROR("960012", "人员导入失败文件生成失败"),
    CHANNEL_USER_STATUS_CHANGED("960013", "人员状态已变化，请刷新后重试"),
    CHANNEL_USER_OCR_RECOGNITION_ERROR("960014", "OCR 识别失败，请稍后重试"),

    EMPLOYEE_RETURN_AMOUNT_EXCEEDED("970001", "员工归还金额大于垫付金额，无法保存"),
    EMPLOYEE_ADVANCE_IMPORT_HEADER_ERROR("970002", "垫付资金导入文件表头不符合"),
    EMPLOYEE_ADVANCE_IMPORT_EMPTY_ERROR("970003", "垫付资金导入文件没有数据"),

    EMPLOYEE_SALARY_RECORD_NOT_EXISTS("980001", "薪资核算记录不存在"),
    EMPLOYEE_SALARY_DETAIL_NOT_EXISTS("980002", "薪资明细不存在"),
    EMPLOYEE_SALARY_STATUS_ERROR("980003", "当前薪资核算状态不允许执行该操作"),
    EMPLOYEE_SALARY_COMPLETED_BILL_REIMPORT_FORBIDDEN("980004", "薪资已核算完成，禁止重新导入厂家账单"),
    EMPLOYEE_SALARY_EMPLOYEE_CODE_DUPLICATE("980005", "同一薪资记录存在重复员工编号"),
    EMPLOYEE_SALARY_FILE_PATH_ERROR("980006", "薪资导入文件路径非法"),
    EMPLOYEE_SALARY_HEADER_ERROR("980007", "薪资导入文件表头不符合"),
    EMPLOYEE_SALARY_EMPTY_FILE_ERROR("980008", "薪资导入文件没有数据"),
    EMPLOYEE_SALARY_WRITE_ERROR("980009", "薪资数据保存失败"),
    EMPLOYEE_SALARY_PAID_BILL_REIMPORT_FORBIDDEN("980010", "薪资已录入实际发放金额，禁止重新导入厂家账单"),

    RESIDENT_PARAM_ERROR("990001", "驻场请求参数非法"),
    RESIDENT_NOT_EXISTS("990002", "驻场人员不存在"),
    RESIDENT_ENTERPRISE_NOT_EXISTS("990003", "企业不存在"),
    RESIDENT_FACTORY_NOT_EXISTS("990004", "工厂不存在、不属于当前企业或不是合作中状态"),
    RESIDENT_ACCOUNT_NOT_EXISTS("990005", "账号不存在、不属于当前企业或不可用"),
    RESIDENT_INSERT_ERROR("990006", "驻场人员新增失败"),
    RESIDENT_UPDATE_ERROR("990007", "驻场人员编辑失败"),
    RESIDENT_AUTH_FAIL("990008", "驻场无权访问或企业上下文不一致"),
    RESIDENT_MOBILE_ERROR("990009", "手机号有误，请重新输入"),
    RESIDENT_ID_CARD_ERROR("990010", "身份证号有误，请重新输入"),
    RESIDENT_DELETE_ERROR("990011", "驻场人员删除失败"),
    RESIDENT_BUSY_ERROR("990012", "驻场记录正在处理中，请稍后重试"),
    RESIDENT_ID_CARD_DUPLICATE("990013", "身份证号已被其他驻场账号使用"),
    RESIDENT_ACCOUNT_DUPLICATE("990014", "账号已存在驻场配置，请使用编辑"),
    RESIDENT_ACCOUNT_FACTORY_DUPLICATE("990015", "账号已绑定该工厂"),
    RESIDENT_ACCOUNT_OTHER_FACTORY_DUPLICATE("990016", "账号已绑定其他工厂");

    private final String code;
    private final String msg;
}
