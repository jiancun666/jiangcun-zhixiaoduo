/*
 Navicat Premium Dump SQL

 Source Server         : finance-center
 Source Server Type    : MySQL
 Source Server Version : 80400 (8.4.0)
 Source Host           : 10.1.3.31:3306
 Source Schema         : zhixiaoduo

 Target Server Type    : MySQL
 Target Server Version : 80400 (8.4.0)
 File Encoding         : 65001

 Date: 01/09/2026 17:51:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for account
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`  (
  `id` bigint NOT NULL COMMENT '主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID，超级用户固定为0',
  `account` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号，只允许数字',
  `password` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加盐MD5密码',
  `password_salt` char(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '6位随机盐',
  `password_version` int NOT NULL DEFAULT 1 COMMENT '密码版本',
  `name` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待激活，1启用中，2已停用',
  `account_type` tinyint NOT NULL DEFAULT 2 COMMENT '账号类型：1平台账号，2企业账号',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_enterprise_id` bigint NULL DEFAULT NULL COMMENT '超级用户上次登录企业ID',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '逻辑删除：1正常，0删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号ID',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `platform_login_account` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci GENERATED ALWAYS AS ((case when ((`account_type` = 1) and (`deleted` = 1)) then `account` else NULL end)) STORED COMMENT '平台账号全局唯一登录名' NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_account_platform_login`(`platform_login_account` ASC) USING BTREE,
  INDEX `idx_account_enterprise_account`(`enterprise_id` ASC, `account` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_account_login`(`account` ASC, `deleted` ASC, `status` ASC) USING BTREE,
  INDEX `idx_account_last_login`(`last_login_time` ASC) USING BTREE,
  INDEX `idx_account_type_enterprise`(`account_type` ASC, `enterprise_id` ASC, `deleted` ASC) USING BTREE,
  CONSTRAINT `chk_account_type_enterprise` CHECK (((`account_type` = 1) and (`enterprise_id` = 0)) or ((`account_type` = 2) and (`enterprise_id` > 0)))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '账号表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for account_role
-- ----------------------------
DROP TABLE IF EXISTS `account_role`;
CREATE TABLE `account_role`  (
  `id` bigint NOT NULL COMMENT '关联主键',
  `enterprise_id` bigint NOT NULL COMMENT '所属企业ID',
  `account_id` bigint NOT NULL COMMENT '账号ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态：0删除，1正常',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_account_role_account`(`enterprise_id` ASC, `account_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_account_role_role`(`enterprise_id` ASC, `role_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账号角色关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for area
-- ----------------------------
DROP TABLE IF EXISTS `area`;
CREATE TABLE `area`  (
  `id` bigint NOT NULL COMMENT '雪花算法生成的主键ID',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '行政区划代码（唯一业务标识）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
  `level` tinyint NOT NULL COMMENT '层级：1-省/自治区/直辖市/特别行政区，2-市/自治州/地区/盟，3-区/县/县级市，4-街道/镇/乡',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '类型描述',
  `parent_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上级行政区划代码',
  `year` int NULL DEFAULT NULL COMMENT '年份',
  `name_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '完整路径',
  `deleted` tinyint(1) NULL DEFAULT 1,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `idx_parent`(`parent_code` ASC) USING BTREE,
  INDEX `idx_level`(`level` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '中国行政区划表（四级）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for channel_account
-- ----------------------------
DROP TABLE IF EXISTS `channel_account`;
CREATE TABLE `channel_account`  (
  `id` bigint NOT NULL,
  `channel_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '渠道id',
  `account_id` bigint NOT NULL COMMENT '账号id',
  `deleted` int NOT NULL DEFAULT 1,
  `create_by` bigint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT 1,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '渠道成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for channel_user
-- ----------------------------
DROP TABLE IF EXISTS `channel_user`;
CREATE TABLE `channel_user`  (
  `id` bigint NOT NULL COMMENT '人员主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业 ID',
  `channel_id` bigint NULL DEFAULT NULL COMMENT '渠道快照 ID',
  `factory_id` bigint NOT NULL COMMENT '所属工厂 ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '人员姓名',
  `contact_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系方式',
  `id_card_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份证号',
  `employee_status` tinyint NOT NULL COMMENT '人员状态',
  `policy_type` tinyint NULL DEFAULT NULL COMMENT '政策类型',
  `user_policy_detail` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人员政策明细',
  `channel_policy_detail` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '渠道政策明细',
  `ethnicity` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '民族',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别',
  `id_card_front_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证正面图片 URL',
  `id_card_back_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证背面图片 URL',
  `transport_type` tinyint NULL DEFAULT NULL COMMENT '交通方式',
  `transport_cost` decimal(14, 2) NULL DEFAULT NULL COMMENT '交通费用',
  `contract_signed_time` datetime NULL DEFAULT NULL COMMENT '合同签署时间',
  `contract_file_urls` json NULL COMMENT '合同文件 URL 数组',
  `employment_date` date NULL DEFAULT NULL COMMENT '入职日期',
  `employee_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '员工编号',
  `payment_method` tinyint NULL DEFAULT NULL COMMENT '收款方式',
  `bank_card_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行卡图片 URL',
  `payee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收款人',
  `bank_card_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行卡号',
  `bank_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属银行',
  `proxy_id_card_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代收人身份证号',
  `proxy_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代收人联系方式',
  `resignation_date` date NULL DEFAULT NULL COMMENT '离职日期',
  `settled` tinyint NULL DEFAULT NULL COMMENT '离职账清标志',
  `settlement_salary` decimal(14, 2) NULL DEFAULT NULL COMMENT '结算工资',
  `insurance_expense` decimal(14, 2) NULL DEFAULT NULL COMMENT '保险支出',
  `performance_expense` decimal(14, 2) NULL DEFAULT NULL COMMENT '绩效支出',
  `abandon_reason` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '放弃入职原因',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '删除标志（1=未删除，0=已删除）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号 ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_channel_user_enterprise_status_time`(`enterprise_id` ASC, `employee_status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_channel_user_enterprise_factory_id_card`(`enterprise_id` ASC, `factory_id` ASC, `id_card_no` ASC) USING BTREE,
  INDEX `idx_channel_user_enterprise_factory_channel_status`(`enterprise_id` ASC, `factory_id` ASC, `channel_id` ASC, `employee_status` ASC) USING BTREE,
  INDEX `idx_channel_user_enterprise_factory_employee_no`(`enterprise_id` ASC, `factory_id` ASC, `employee_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人员主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for channel_user_status_record
-- ----------------------------
DROP TABLE IF EXISTS `channel_user_status_record`;
CREATE TABLE `channel_user_status_record`  (
  `id` bigint NOT NULL COMMENT '状态记录主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业 ID',
  `channel_user_id` bigint NOT NULL COMMENT '人员 ID',
  `start_status` tinyint NULL DEFAULT NULL COMMENT '开始状态',
  `end_status` tinyint NOT NULL COMMENT '结束状态',
  `detail_json` json NOT NULL COMMENT '状态变更快照',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '删除标志（1=未删除，0=已删除）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号 ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_channel_user_status_record_enterprise_user_time`(`enterprise_id` ASC, `channel_user_id` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '人员状态记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_advance
-- ----------------------------
DROP TABLE IF EXISTS `employee_advance`;
CREATE TABLE `employee_advance`  (
  `id` bigint NOT NULL COMMENT '资金交易主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `channel_id` bigint NULL DEFAULT NULL COMMENT '所属渠道ID',
  `channel_user_id` bigint NOT NULL COMMENT '所属人员ID',
  `business_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '业务类型：companyAdvance公司垫付、\r\n         employeeReturn员工归还、\r\n         accountSettled人走账清、\r\n         salaryRecovery工资扣回',
  `cost_type` tinyint NULL DEFAULT NULL COMMENT '费用类型：1车费、2体检费、3工资预支、4住宿费、5其他',
  `trans_type` tinyint NOT NULL COMMENT '交易类型：1垫付、2归还',
  `amount` decimal(14, 2) NOT NULL COMMENT '交易金额，统一保存正数',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '备注',
  `source_type` tinyint NOT NULL DEFAULT 1 COMMENT '数据来源：1手工新增、2Excel导入',
  `import_record_id` bigint NULL DEFAULT NULL COMMENT '导入记录ID，手工新增时为空',
  `import_row_no` int NULL DEFAULT NULL COMMENT 'Excel原始行号，手工新增时为空',
  `transaction_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易流水号，用于审计和接口幂等',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '逻辑删除：1正常、0删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_employee_advance_transaction_no`(`enterprise_id` ASC, `transaction_no` ASC) USING BTREE,
  INDEX `idx_employee_advance_user_flow`(`enterprise_id` ASC, `channel_user_id` ASC, `cost_type` ASC, `trans_type` ASC, `deleted` ASC, `create_time` ASC, `id` ASC) USING BTREE,
  INDEX `idx_employee_advance_channel_time`(`enterprise_id` ASC, `channel_id` ASC, `deleted` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_employee_advance_import`(`enterprise_id` ASC, `import_record_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '渠道垫付资金交易主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_advance_allocation
-- ----------------------------
DROP TABLE IF EXISTS `employee_advance_allocation`;
CREATE TABLE `employee_advance_allocation`  (
  `id` bigint NOT NULL COMMENT '分配明细主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `repayment_id` bigint NOT NULL COMMENT '归还交易ID，关联employee_advance.id，且trans_type=2',
  `advance_id` bigint NOT NULL COMMENT '被扣减的垫付交易ID，关联employee_advance.id，且trans_type=1',
  `allocated_amount` decimal(14, 2) NOT NULL COMMENT '本次从该笔垫付中扣减的金额',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '逻辑删除：1正常、0删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_advance_allocation_relation`(`repayment_id` ASC, `advance_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_advance_allocation_advance`(`enterprise_id` ASC, `advance_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_advance_allocation_repayment`(`enterprise_id` ASC, `repayment_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '垫付资金归还分配明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_channel
-- ----------------------------
DROP TABLE IF EXISTS `employee_channel`;
CREATE TABLE `employee_channel`  (
  `id` bigint NOT NULL,
  `response_name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '负责人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系方式',
  `business_license_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '营业执照',
  `channel_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '渠道名称',
  `cooperation_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '合作方式 1 长线 2 短线.多个以逗号分割',
  `settle_acct_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结算账户名称',
  `enterprise_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企业id',
  `settle_acct_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结算账户卡号',
  `settle_acct_bank_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结算账户开户行',
  `deleted` int NOT NULL DEFAULT 1,
  `create_by` bigint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT 1,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '渠道管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_channel_bill
-- ----------------------------
DROP TABLE IF EXISTS `employee_channel_bill`;
CREATE TABLE `employee_channel_bill`  (
  `id` bigint NOT NULL,
  `channel_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '所属渠道',
  `settle_month` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结算月份 yyyy-MM',
  `settle_amount` decimal(14, 2) NOT NULL COMMENT '结算金额',
  `bill_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账单类型 1 长线账单 2 短线账单',
  `bill_detail_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账单明细 上传的文件地址',
  `bill_detail_file_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '账单明细 上传的文件名称',
  `settle_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结算状态 1 待确认 2 待结算 3 已结算',
  `enterprise_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '企业id',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备注',
  `deleted` int NOT NULL DEFAULT 1,
  `create_by` bigint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT 1,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '渠道管理' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_channel_bill_detail
-- ----------------------------
DROP TABLE IF EXISTS `employee_channel_bill_detail`;
CREATE TABLE `employee_channel_bill_detail`  (
  `id` bigint NOT NULL,
  `channel_bill_id` bigint NOT NULL COMMENT '渠道账单表id',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户姓名',
  `id_card_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '身份证号',
  `settle_status` tinyint NOT NULL COMMENT '结算状态 1 待确认 2 待结算 3 已结算',
  `settle_month` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '结算月份 yyyy-MM',
  `deleted` int NOT NULL DEFAULT 1,
  `create_by` bigint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` bigint NULL DEFAULT 1,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '渠道账单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_salary_detail
-- ----------------------------
DROP TABLE IF EXISTS `employee_salary_detail`;
CREATE TABLE `employee_salary_detail`  (
  `id` bigint NOT NULL COMMENT '主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业 ID',
  `salary_record_id` bigint NOT NULL COMMENT '薪资核算记录 ID',
  `bill_detail_id` bigint NOT NULL COMMENT '厂家账单明细 ID',
  `channel_user_id` bigint NULL DEFAULT NULL COMMENT '匹配人员 ID',
  `match_status` tinyint NOT NULL COMMENT '人员匹配状态（1=已匹配，2=未匹配）',
  `employee_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '员工编号',
  `employee_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账单员工姓名',
  `unit_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账单单位名称',
  `bill_hourly_rate` decimal(18, 2) NULL DEFAULT NULL COMMENT '账单小时单价',
  `performance_score` decimal(18, 2) NULL DEFAULT NULL COMMENT '账单绩效分数',
  `service_hours` decimal(18, 2) NULL DEFAULT NULL COMMENT '账单工时',
  `bill_expense_subtotal` decimal(18, 2) NULL DEFAULT NULL COMMENT '账单费用小计',
  `comprehensive_assessment_fee` decimal(18, 2) NULL DEFAULT NULL COMMENT '账单综合考核费',
  `bill_payable_total` decimal(18, 2) NULL DEFAULT NULL COMMENT '账单应付费用合计',
  `bill_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '账单备注',
  `employee_unit_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '员工单价',
  `handling_fee` decimal(18, 2) NULL DEFAULT NULL COMMENT '手续费',
  `management_fee` decimal(18, 2) NULL DEFAULT NULL COMMENT '管理费',
  `individual_income_tax` decimal(18, 2) NULL DEFAULT NULL COMMENT '个人所得税',
  `salary_remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '薪资备注',
  `actual_paid_amount` decimal(18, 2) NULL DEFAULT NULL COMMENT '实际发放金额',
  `user_name_snapshot` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人员姓名快照',
  `id_card_no_snapshot` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号快照',
  `channel_name_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '渠道名称快照',
  `employee_status_snapshot` tinyint NULL DEFAULT NULL COMMENT '人员状态快照',
  `policy_type_snapshot` tinyint NULL DEFAULT NULL COMMENT '政策类型快照',
  `user_policy_detail_snapshot` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '人员政策明细快照',
  `channel_policy_detail_snapshot` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '渠道政策明细快照',
  `payment_method_snapshot` tinyint NULL DEFAULT NULL COMMENT '收款方式快照',
  `payee_name_snapshot` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收款人快照',
  `bank_card_no_snapshot` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行卡号快照',
  `bank_name_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开户行快照',
  `proxy_id_card_no_snapshot` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代收人身份证号快照',
  `proxy_phone_snapshot` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '代收人联系电话快照',
  `insurance_amount_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '保险金额快照',
  `wage_advance_amount_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '工资预支金额快照',
  `transport_cost_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '交通费用快照',
  `medical_accommodation_amount_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '体检住宿费用快照',
  `account_settled_amount_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '人走账清金额快照',
  `total_advance_amount_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '垫付总额快照',
  `salary_subtotal_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '薪资小计快照',
  `salary_payable_amount_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '薪资应发金额快照',
  `salary_net_amount_snapshot` decimal(18, 2) NULL DEFAULT NULL COMMENT '薪资实发金额快照',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号 ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号 ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '删除标志（1=未删除，0=已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_salary_employee`(`enterprise_id` ASC, `salary_record_id` ASC, `employee_code` ASC) USING BTREE,
  INDEX `idx_salary_paid`(`enterprise_id` ASC, `salary_record_id` ASC, `actual_paid_amount` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工薪资核算明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for employee_salary_record
-- ----------------------------
DROP TABLE IF EXISTS `employee_salary_record`;
CREATE TABLE `employee_salary_record`  (
  `id` bigint NOT NULL COMMENT '主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业 ID',
  `bill_import_record_id` bigint NOT NULL COMMENT '厂家账单导入记录 ID',
  `bill_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单月份，格式 yyyy-MM',
  `factory_id` bigint NOT NULL COMMENT '工厂 ID',
  `factory_name_snapshot` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '核算完成后的工厂名称快照',
  `calculation_status` tinyint NOT NULL COMMENT '核算状态（1=核算中，2=核算完成）',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '核算完成时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号 ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号 ID',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '删除标志（1=未删除，0=已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_salary_scope`(`enterprise_id` ASC, `bill_month` ASC, `factory_id` ASC) USING BTREE,
  INDEX `idx_salary_status`(`enterprise_id` ASC, `calculation_status` ASC, `create_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '员工薪资核算记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for enterprise
-- ----------------------------
DROP TABLE IF EXISTS `enterprise`;
CREATE TABLE `enterprise`  (
  `id` bigint NOT NULL COMMENT '主键',
  `enterprise_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业名称',
  `enterprise_short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '企业简称',
  `contact_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人电话',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '逻辑删除：1正常，0删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人账号ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号ID',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_enterprise_name_deleted`(`enterprise_name` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_enterprise_short_name_deleted`(`enterprise_short_name` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '企业表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for export_record
-- ----------------------------
DROP TABLE IF EXISTS `export_record`;
CREATE TABLE `export_record`  (
  `id` bigint NOT NULL COMMENT '主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID，系统管理范围使用0',
  `export_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导出类型编码',
  `export_content` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导出内容名称',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户下载文件名',
  `file_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'TOS文件完整可访问URL',
  `request_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导出条件JSON快照',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '可选的导出模块功能权限编码',
  `data_permission_snapshot` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提交时数据权限快照JSON',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待导出，1导出中，2导出完成，3导出失败',
  `exported_count` int NOT NULL DEFAULT 0 COMMENT '已写入数据数量',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小，单位字节',
  `task_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务级提示信息',
  `worker_id` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '本次执行唯一标识',
  `heartbeat_time` datetime NULL DEFAULT NULL COMMENT '最近任务心跳',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `resume_count` int NOT NULL DEFAULT 0 COMMENT '中断恢复次数',
  `super_user` tinyint NOT NULL DEFAULT 0 COMMENT '提交任务时是否为超级用户：1是，0否',
  `client_type` tinyint NOT NULL DEFAULT 1 COMMENT '提交客户端：1 PC端，2移动端',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '逻辑删除：1正常，0删除',
  `create_by` bigint NULL DEFAULT NULL COMMENT '导出账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导出提交时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_export_enterprise_time`(`enterprise_id` ASC, `create_time` ASC, `id` ASC) USING BTREE,
  INDEX `idx_export_status_heartbeat`(`status` ASC, `heartbeat_time` ASC) USING BTREE,
  INDEX `idx_export_type_status`(`export_type` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Excel异步导出任务记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for factory
-- ----------------------------
DROP TABLE IF EXISTS `factory`;
CREATE TABLE `factory`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `factory_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂名称',
  `factory_location_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂所在地行政区划编码',
  `factory_status` tinyint NOT NULL DEFAULT 1 COMMENT '工厂状态：1-合作中，0-暂停合作',
  `deleted` tinyint(1) NOT NULL DEFAULT 1 COMMENT '删除标志（1=未删除，0=已删除）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工厂表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for factory_bill_import_detail
-- ----------------------------
DROP TABLE IF EXISTS `factory_bill_import_detail`;
CREATE TABLE `factory_bill_import_detail`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `import_record_id` bigint NOT NULL COMMENT '导入记录ID',
  `unit_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位',
  `employee_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编号',
  `employee_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `hourly_rate` decimal(18, 2) NOT NULL COMMENT '小时单价',
  `performance_score` decimal(18, 2) NOT NULL COMMENT '绩效分数',
  `work_hours` decimal(18, 2) NOT NULL COMMENT '工时（小时）',
  `expense_subtotal` decimal(18, 2) NOT NULL COMMENT '费用小计',
  `comprehensive_assessment_fee` decimal(18, 2) NOT NULL COMMENT '综合考核费',
  `payable_total` decimal(18, 2) NOT NULL COMMENT '应付费用合计',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_enterprise_record`(`enterprise_id` ASC, `import_record_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '厂家账单导入明细表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for factory_bill_import_record
-- ----------------------------
DROP TABLE IF EXISTS `factory_bill_import_record`;
CREATE TABLE `factory_bill_import_record`  (
  `id` bigint NOT NULL COMMENT 'ID',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `bill_month` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账单月份（yyyy-MM）',
  `factory_id` bigint NOT NULL COMMENT '工厂ID',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件相对URL',
  `failed_file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败文件相对URL',
  `deleted` tinyint(1) NOT NULL DEFAULT 1 COMMENT '删除标志（1=未删除，0=已删除）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_enterprise_month_factory`(`enterprise_id` ASC, `bill_month` ASC, `factory_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '厂家账单导入记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for import_record
-- ----------------------------
DROP TABLE IF EXISTS `import_record`;
CREATE TABLE `import_record`  (
  `id` bigint NOT NULL COMMENT '主键',
  `enterprise_id` bigint NOT NULL COMMENT '企业ID',
  `import_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导入类型编码',
  `import_type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '导入类型名称',
  `import_file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始导入文件名',
  `source_file_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '本地上传或远程文件访问地址',
  `request_params` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '导入模块前端参数JSON快照',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '可选的导入模块功能权限编码',
  `super_user` tinyint NOT NULL DEFAULT 0 COMMENT '提交时是否为超级用户',
  `client_type` tinyint NOT NULL DEFAULT 1 COMMENT '提交客户端：1 PC端，2移动端',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待导入，1导入中，2导入完成，3导入失败',
  `total_count` int NOT NULL DEFAULT 0 COMMENT '总数据量',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数据量',
  `failure_count` int NOT NULL DEFAULT 0 COMMENT '失败数据量',
  `failure_file_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '导入失败明细TOS完整访问URL',
  `failure_file_status` tinyint NOT NULL DEFAULT 0 COMMENT '失败文件状态：0无，1生成中，2可下载，3生成失败',
  `task_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务级提示信息',
  `worker_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '执行实例标识',
  `heartbeat_time` datetime NULL DEFAULT NULL COMMENT '任务心跳时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '逻辑删除：1正常，0删除',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '创建人账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '更新人账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_import_type_status_time`(`import_type` ASC, `status` ASC, `create_time` ASC, `id` ASC) USING BTREE,
  INDEX `idx_import_enterprise_time`(`enterprise_id` ASC, `create_time` ASC, `id` ASC) USING BTREE,
  INDEX `idx_import_heartbeat`(`status` ASC, `heartbeat_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Excel导入任务记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for menu_data_scope
-- ----------------------------
DROP TABLE IF EXISTS `menu_data_scope`;
CREATE TABLE `menu_data_scope`  (
  `id` bigint NOT NULL COMMENT '关联主键',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `scope_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '该菜单允许配置的数据权限编码',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态：0删除，1正常',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_menu_data_scope`(`menu_id` ASC, `scope_code` ASC) USING BTREE,
  INDEX `idx_menu_data_scope_menu`(`menu_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单可配置数据权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for menu_info
-- ----------------------------
DROP TABLE IF EXISTS `menu_info`;
CREATE TABLE `menu_info`  (
  `id` bigint NOT NULL COMMENT '菜单主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父节点ID，顶级为0',
  `menu_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单或功能权限编码',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '节点类型：1目录，2菜单，3按钮',
  `client_type` tinyint NOT NULL DEFAULT 1 COMMENT '客户端类型：1 PC端，2移动端',
  `route_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前端路由地址',
  `component_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '前端组件地址',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否展示：1展示，0隐藏',
  `enabled` tinyint NOT NULL DEFAULT 1 COMMENT '是否启用：1启用，0停用',
  `data_resource_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '数据权限资源编码',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态：0删除，1正常',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_menu_info_client_code`(`client_type` ASC, `menu_code` ASC) USING BTREE,
  INDEX `idx_menu_info_parent`(`parent_id` ASC, `deleted` ASC, `enabled` ASC, `sort_no` ASC) USING BTREE,
  INDEX `idx_menu_info_client_parent`(`client_type` ASC, `parent_id` ASC, `deleted` ASC, `enabled` ASC, `sort_no` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单及功能权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resident_factory
-- ----------------------------
DROP TABLE IF EXISTS `resident_factory`;
CREATE TABLE `resident_factory`  (
  `id` bigint NOT NULL COMMENT '驻场账号工厂关联ID',
  `account_id` bigint NOT NULL COMMENT '关联企业账号ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '驻场人员姓名',
  `phone` char(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '驻场人员电话，只可输入数字',
  `id_card` char(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '驻场人员身份证号，18位数字，需校验，不可输入中文',
  `factory_id` bigint NOT NULL COMMENT '所属工厂id',
  `create_by` bigint NULL DEFAULT NULL COMMENT '账号创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '最后修改人',
  `update_time` datetime NOT NULL COMMENT '最后修改时间',
  `deleted` tinyint NOT NULL,
  `enterprise_id` bigint NOT NULL COMMENT '所属企业ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resident_account_factory`(`enterprise_id` ASC, `account_id` ASC, `factory_id` ASC) USING BTREE,
  INDEX `idx_resident_account_scope`(`enterprise_id` ASC, `account_id` ASC, `deleted` ASC, `factory_id` ASC) USING BTREE,
  INDEX `idx_resident_id_card`(`enterprise_id` ASC, `id_card` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for role_info
-- ----------------------------
DROP TABLE IF EXISTS `role_info`;
CREATE TABLE `role_info`  (
  `id` bigint NOT NULL COMMENT '角色主键',
  `enterprise_id` bigint NOT NULL COMMENT '所属企业ID',
  `role_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '稳定角色编码',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_type` tinyint NOT NULL COMMENT '角色类型：1超级管理员，2驻场，3渠道，4自定义',
  `built_in` tinyint NOT NULL DEFAULT 0 COMMENT '是否内置：1是，0否',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态：0删除，1正常',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_info_enterprise`(`enterprise_id` ASC, `deleted` ASC, `role_type` ASC) USING BTREE,
  INDEX `idx_role_info_code`(`enterprise_id` ASC, `role_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_role_info_name`(`enterprise_id` ASC, `role_name` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for role_menu
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu`  (
  `id` bigint NOT NULL COMMENT '关联主键',
  `enterprise_id` bigint NOT NULL COMMENT '所属企业ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态：0删除，1正常',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_menu_role`(`enterprise_id` ASC, `role_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_role_menu_menu`(`menu_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for role_menu_data_scope
-- ----------------------------
DROP TABLE IF EXISTS `role_menu_data_scope`;
CREATE TABLE `role_menu_data_scope`  (
  `id` bigint NOT NULL COMMENT '关联主键',
  `enterprise_id` bigint NOT NULL COMMENT '所属企业ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '数据权限所属菜单ID',
  `scope_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '数据权限编码',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态：0删除，1正常',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_scope_role`(`enterprise_id` ASC, `role_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_role_scope_menu`(`menu_id` ASC, `scope_code` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色菜单数据权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tos_upload_task
-- ----------------------------
DROP TABLE IF EXISTS `tos_upload_task`;
CREATE TABLE `tos_upload_task`  (
  `id` bigint NOT NULL COMMENT '任务主键',
  `enterprise_id` bigint NOT NULL COMMENT '所属企业ID',
  `storage_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '存储模式：TOS、LOCAL_AND_TOS',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户上传时的原始文件名',
  `stored_filename` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务端生成的唯一文件名',
  `local_relative_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '双写模式本地正式文件相对路径',
  `staging_relative_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'TOS-only待上传文件相对路径',
  `bucket_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务创建时的TOS桶名快照',
  `object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'TOS对象名称',
  `tos_file_url` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '上传成功后的稳定访问地址',
  `content_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '客户端上报的MIME类型',
  `file_size` bigint NOT NULL COMMENT '实际文件大小，单位字节',
  `uploaded_size` bigint NOT NULL DEFAULT 0 COMMENT '已上传字节数',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待上传，1上传中，2完成，3失败',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '任务级失败次数',
  `task_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户可见任务消息',
  `request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'TOS请求ID',
  `etag` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'TOS对象ETag',
  `worker_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前执行权唯一标识',
  `heartbeat_time` datetime NULL DEFAULT NULL COMMENT '最近上传进度时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '首次开始上传时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '任务结束时间',
  `deleted` tinyint NOT NULL DEFAULT 1 COMMENT '记录状态：0不可用，1可用',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建账号ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新账号ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tos_upload_task_object`(`bucket_name` ASC, `object_key` ASC) USING BTREE,
  INDEX `idx_tos_upload_task_schedule`(`deleted` ASC, `status` ASC, `create_time` ASC, `id` ASC) USING BTREE,
  INDEX `idx_tos_upload_task_stale`(`deleted` ASC, `status` ASC, `heartbeat_time` ASC) USING BTREE,
  INDEX `idx_tos_upload_task_enterprise`(`enterprise_id` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'TOS异步上传任务' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
