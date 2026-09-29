package com.semple.zhixiaoduo.permission;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.semple.zhixiaoduo.enums.DataResourceTypeEnum;
import com.semple.zhixiaoduo.enums.DataScopeTypeEnum;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * MyBatis-Plus 数据权限条件生成器。
 */
@Component
@RequiredArgsConstructor
public class PermissionDataHandler implements MultiDataPermissionHandler {

    /**
     * 复杂查询目标别名解析器。
     */
    private final PermissionTargetResolver targetResolver;

    /**
     * 仅为当前权限资源的主表生成附加条件，其他联表和子查询不处理。
     *
     * @param table table 参数。
     * @param where where 参数。
     * @param mappedStatementId 业务记录 ID。
     * @return 处理结果。
     */
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        DataPermissionDecision decision = DataPermissionContextHolder.get();
        if (decision == null || decision.getResource() == null
                || !decision.getResource().getTableName().equalsIgnoreCase(table.getName())) {
            return null;
        }

        Set<String> targets = targetResolver.resolve(mappedStatementId);
        String alias = table.getAlias() == null ? null : table.getAlias().getName();
        if (!targets.isEmpty() && (!StringUtils.hasText(alias) || !targets.contains(alias))) {
            return null;
        }

        String qualifier = StringUtils.hasText(alias) ? alias : table.getName();
        validateIdentifier(qualifier);
        String condition = buildCondition(qualifier, decision);
        try {
            return CCJSqlParserUtil.parseCondExpression(condition);
        } catch (Exception exception) {
            throw new IllegalStateException("数据权限SQL生成失败，permissionCode="
                    + decision.getPermissionCode(), exception);
        }
    }

    /**
     * 根据资源字段和多角色并集结果生成企业条件及数据范围条件。
     * <p>普通业务资源始终限定当前企业；企业表自身没有 enterprise_id，全部数据范围不追加企业条件。
     * 多个数据权限范围使用 OR 连接，没有有效范围时返回恒假条件。</p>
     *
     * @param qualifier qualifier 参数。
     * @param decision decision 参数。
     * @return 处理结果。
     */
    private String buildCondition(String qualifier, DataPermissionDecision decision) {
        DataResourceTypeEnum resource = decision.getResource();
        Long enterpriseId = decision.getEnterpriseId();
        Long accountId = decision.getAccountId();
        // 企业表自身是跨企业资源，没有 enterprise_id；其他业务资源仍必须隔离当前企业。
        String enterpriseCondition = StringUtils.hasText(resource.getEnterpriseColumn())
                ? qualifier + "." + resource.getEnterpriseColumn() + " = "
                        + formatEnterpriseId(resource, enterpriseId)
                : "1 = 1";
        if (decision.isAllData()) {
            return enterpriseCondition;
        }

        List<String> scopeConditions = new ArrayList<>();
        for (DataScopeTypeEnum scope : decision.getScopes()) {
            switch (scope) {
                case RESPONSIBLE_FACTORY -> addFactoryCondition(scopeConditions, qualifier, resource,
                        enterpriseId, accountId);
                case OWN_CHANNEL -> addChannelCondition(scopeConditions, qualifier, resource,
                        enterpriseId, accountId);
                case SELF_CREATED, SELF_IMPORTED, SELF_EXPORTED -> addCreatorCondition(scopeConditions,
                        qualifier, resource, accountId);
                case ALL -> {
                    return enterpriseCondition;
                }
            }
        }
        if (scopeConditions.isEmpty()) {
            return enterpriseCondition + " AND 1 = 0";
        }
        return enterpriseCondition + " AND (" + String.join(" OR ", scopeConditions) + ")";
    }

    /**
     * 为“负责工厂”范围追加账号负责工厂的 EXISTS 条件。
     *
     * @param conditions conditions 参数。
     * @param qualifier qualifier 参数。
     * @param resource resource 参数。
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     */
    private void addFactoryCondition(List<String> conditions, String qualifier, DataResourceTypeEnum resource,
                                     Long enterpriseId, Long accountId) {
        if (!StringUtils.hasText(resource.getFactoryColumn())) {
            return;
        }
        // 驻场表每条有效记录表示账号负责一个工厂，同一账号可通过多条记录负责多个工厂。
        conditions.add("EXISTS (SELECT 1 FROM resident_factory permission_resident"
                + " WHERE permission_resident.enterprise_id = '" + enterpriseId + "'"
                + " AND permission_resident.account_id = " + accountId
                + " AND permission_resident.factory_id = " + qualifier + "." + resource.getFactoryColumn()
                + " AND permission_resident.deleted = 1)");
    }

    /**
     * 为“所属渠道”范围追加账号所属渠道的 EXISTS 条件。
     *
     * @param conditions conditions 参数。
     * @param qualifier qualifier 参数。
     * @param resource resource 参数。
     * @param enterpriseId 当前企业 ID。
     * @param accountId 业务记录 ID。
     */
    private void addChannelCondition(List<String> conditions, String qualifier, DataResourceTypeEnum resource,
                                     Long enterpriseId, Long accountId) {
        if (!StringUtils.hasText(resource.getChannelColumn())) {
            return;
        }
        // channel_account 当前没有 enterprise_id，通过 employee_channel 校验关系属于当前企业。
        conditions.add("EXISTS (SELECT 1 FROM channel_account permission_channel_account"
                + " INNER JOIN employee_channel permission_channel"
                + " ON permission_channel.id = permission_channel_account.channel_id"
                + " AND permission_channel.enterprise_id = " + enterpriseId
                + " AND permission_channel.deleted = 1"
                + " WHERE permission_channel_account.account_id = " + accountId
                + " AND permission_channel_account.channel_id = " + qualifier + "." + resource.getChannelColumn()
                + " AND permission_channel_account.deleted = 1)");
    }

    /**
     * 为本人创建、本人导入和本人导出范围追加创建账号条件。
     *
     * @param conditions conditions 参数。
     * @param qualifier qualifier 参数。
     * @param resource resource 参数。
     * @param accountId 业务记录 ID。
     */
    private void addCreatorCondition(List<String> conditions, String qualifier, DataResourceTypeEnum resource,
                                     Long accountId) {
        if (StringUtils.hasText(resource.getCreatorColumn())) {
            conditions.add(qualifier + "." + resource.getCreatorColumn() + " = " + accountId);
        }
    }

    /**
     * resident_factory 的 enterprise_id 为 varchar，必须按字符串精确比较，避免 19 位 ID 转浮点后串企业。
     */
    private String formatEnterpriseId(DataResourceTypeEnum resource, Long enterpriseId) {
        return resource == DataResourceTypeEnum.RESIDENT ? "'" + enterpriseId + "'" : String.valueOf(enterpriseId);
    }

    /**
     * 别名来自代码注解，但仍限制为普通 SQL 标识符，防止错误配置形成危险 SQL。
     *
     * @param identifier identifier 参数。
     */
    private void validateIdentifier(String identifier) {
        if (!identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalStateException("非法的数据权限表别名：" + identifier);
        }
    }
}
