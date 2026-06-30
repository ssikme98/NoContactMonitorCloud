package com.ruoyi.nocontact.common;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.SecurityConstants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.common.core.context.SecurityContextHolder;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.common.core.web.domain.BaseEntity;
import com.ruoyi.common.security.utils.SecurityUtils;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import com.ruoyi.system.api.model.LoginUser;
import java.util.ArrayList;
import java.util.List;

/**
 * 无感业务模块的数据范围过滤。
 */
public final class NocontactDataScopeHelper
{
    private static final String DATA_SCOPE = "dataScope";

    private NocontactDataScopeHelper()
    {
    }

    public static void applyDataScope(BaseEntity entity, String deptAlias, String deptField, String userAlias,
            String userField)
    {
        applyDataScope(entity, deptAlias, deptField, userAlias, userField, false);
    }

    public static void applyDeptDataScope(BaseEntity entity, String deptAlias, String deptField)
    {
        applyDataScope(entity, deptAlias, deptField, "", "", true);
    }

    private static void applyDataScope(BaseEntity entity, String deptAlias, String deptField, String userAlias,
            String userField, boolean selfAsDept)
    {
        if (entity == null)
        {
            return;
        }
        entity.getParams().put(DATA_SCOPE, "");
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getSysUser() == null)
        {
            return;
        }
        SysUser user = loginUser.getSysUser();
        if (user.isAdmin())
        {
            return;
        }
        String scopeSql = buildDataScopeSql(user, deptAlias, deptField, userAlias, userField,
                SecurityContextHolder.get(SecurityConstants.ROLE_PERMISSION), selfAsDept);
        entity.getParams().put(DATA_SCOPE, scopeSql);
    }

    static String buildDataScopeSql(SysUser user, String deptAlias, String deptField, String userAlias,
            String userField, String permission)
    {
        return buildDataScopeSql(user, deptAlias, deptField, userAlias, userField, permission, false);
    }

    private static String buildDataScopeSql(SysUser user, String deptAlias, String deptField, String userAlias,
            String userField, String permission, boolean selfAsDept)
    {
        StringBuilder sqlString = new StringBuilder();
        List<String> conditions = new ArrayList<String>();
        List<String> customRoleIds = new ArrayList<String>();
        if (StringUtils.isNotEmpty(user.getRoles()))
        {
            for (SysRole role : user.getRoles())
            {
                if (Constants.Dept.DATA_SCOPE_CUSTOM.equals(role.getDataScope())
                        && StringUtils.equals(role.getStatus(), UserConstants.ROLE_NORMAL)
                        && role.getRoleId() != null && roleMatchesPermission(role, permission))
                {
                    customRoleIds.add(Convert.toStr(role.getRoleId()));
                }
            }
        }

        if (StringUtils.isNotEmpty(user.getRoles()))
        {
            for (SysRole role : user.getRoles())
            {
                String dataScope = role.getDataScope();
                if (StringUtils.isBlank(dataScope) || conditions.contains(dataScope)
                        || StringUtils.equals(role.getStatus(), UserConstants.ROLE_DISABLE)
                        || !roleMatchesPermission(role, permission))
                {
                    continue;
                }
                if (Constants.Dept.DATA_SCOPE_ALL.equals(dataScope))
                {
                    return "";
                }
                else if (Constants.Dept.DATA_SCOPE_CUSTOM.equals(dataScope))
                {
                    appendCustomScope(sqlString, deptAlias, deptField, role, customRoleIds);
                }
                else if (Constants.Dept.DATA_SCOPE_DEPT.equals(dataScope))
                {
                    sqlString.append(StringUtils.format(" OR {}.{} = {} ", deptAlias, deptField, safeLong(user.getDeptId())));
                }
                else if (Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope))
                {
                    Long deptId = safeLong(user.getDeptId());
                    sqlString.append(StringUtils.format(
                            " OR {}.{} IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} OR ',' || ancestors || ',' LIKE '%,{},%' ) ",
                            deptAlias, deptField, deptId, deptId));
                }
                else if (Constants.Dept.DATA_SCOPE_SELF.equals(dataScope))
                {
                    if (selfAsDept)
                    {
                        sqlString.append(StringUtils.format(" OR {}.{} = {} ", deptAlias, deptField,
                                safeLong(user.getDeptId())));
                    }
                    else
                    {
                        appendSelfScope(sqlString, user, deptAlias, deptField, userAlias, userField);
                    }
                }
                else
                {
                    continue;
                }
                conditions.add(dataScope);
            }
        }

        if (conditions.isEmpty())
        {
            appendNoDataScope(sqlString, deptAlias, deptField);
        }
        if (StringUtils.isBlank(sqlString.toString()))
        {
            return "";
        }
        return " AND (" + sqlString.substring(4) + ")";
    }

    private static void appendCustomScope(StringBuilder sqlString, String deptAlias, String deptField, SysRole role,
            List<String> customRoleIds)
    {
        if (customRoleIds.size() > 1)
        {
            sqlString.append(StringUtils.format(
                    " OR {}.{} IN ( SELECT dept_id FROM sys_role_dept WHERE role_id IN ({}) ) ", deptAlias, deptField,
                    String.join(",", customRoleIds)));
        }
        else if (role.getRoleId() != null)
        {
            sqlString.append(StringUtils.format(
                    " OR {}.{} IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias, deptField,
                    role.getRoleId()));
        }
    }

    private static void appendSelfScope(StringBuilder sqlString, SysUser user, String deptAlias, String deptField,
            String userAlias, String userField)
    {
        if (StringUtils.isNotBlank(userAlias) && StringUtils.isNotBlank(userField))
        {
            String username = StringUtils.defaultIfBlank(user.getUserName(), SecurityUtils.getUsername());
            if (StringUtils.isNotBlank(username))
            {
                sqlString.append(StringUtils.format(" OR {}.{} = '{}' ", userAlias, userField, escapeSql(username)));
                return;
            }
        }
        appendNoDataScope(sqlString, deptAlias, deptField);
    }

    private static void appendNoDataScope(StringBuilder sqlString, String deptAlias, String deptField)
    {
        sqlString.append(StringUtils.format(" OR {}.{} = 0 ", deptAlias, deptField));
    }

    private static boolean roleMatchesPermission(SysRole role, String permission)
    {
        return StringUtils.isEmpty(permission) || StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission));
    }

    private static Long safeLong(Long value)
    {
        return value == null ? 0L : value;
    }

    private static String escapeSql(String value)
    {
        return value.replace("'", "''");
    }
}
