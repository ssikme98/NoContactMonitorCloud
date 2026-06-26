package com.ruoyi.nocontact.common;

import com.ruoyi.common.core.constant.Constants;
import com.ruoyi.common.core.constant.UserConstants;
import com.ruoyi.system.api.domain.SysRole;
import com.ruoyi.system.api.domain.SysUser;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NocontactDataScopeHelperTest
{
    @Test
    void deptScopeUsesCurrentUserDept()
    {
        String sql = NocontactDataScopeHelper.buildDataScopeSql(userWithRole(Constants.Dept.DATA_SCOPE_DEPT), "b",
                "dept_id", "b", "create_by", "fusion:collection:list");

        assertTrue(sql.contains("b.dept_id = 88"));
    }

    @Test
    void childDeptScopeUsesPostgresAncestorMatch()
    {
        String sql = NocontactDataScopeHelper.buildDataScopeSql(
                userWithRole(Constants.Dept.DATA_SCOPE_DEPT_AND_CHILD), "b", "dept_id", "b", "create_by",
                "fusion:collection:list");

        assertTrue(sql.contains("',' || ancestors || ',' LIKE '%,88,%'"));
    }

    @Test
    void customScopeUsesRoleDeptMapping()
    {
        String sql = NocontactDataScopeHelper.buildDataScopeSql(userWithRole(Constants.Dept.DATA_SCOPE_CUSTOM), "f",
                "dept_id", "f", "create_by", "fusion:collection:list");

        assertTrue(sql.contains("f.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = 20 )"));
    }

    @Test
    void selfScopeUsesCreateBy()
    {
        String sql = NocontactDataScopeHelper.buildDataScopeSql(userWithRole(Constants.Dept.DATA_SCOPE_SELF), "m",
                "dept_id", "m", "create_by", "warning:message:list");

        assertTrue(sql.contains("m.create_by = 'scope-user'"));
    }

    @Test
    void allScopeReturnsEmptySql()
    {
        String sql = NocontactDataScopeHelper.buildDataScopeSql(userWithRole(Constants.Dept.DATA_SCOPE_ALL), "m",
                "dept_id", "m", "create_by", "warning:message:list");

        assertEquals("", sql);
    }

    private SysUser userWithRole(String dataScope)
    {
        SysRole role = new SysRole();
        role.setRoleId(20L);
        role.setDataScope(dataScope);
        role.setStatus(UserConstants.ROLE_NORMAL);
        role.setPermissions(new HashSet<String>(Arrays.asList("fusion:collection:list", "warning:message:list")));
        SysUser user = new SysUser();
        user.setUserId(2L);
        user.setUserName("scope-user");
        user.setDeptId(88L);
        user.setRoles(Collections.singletonList(role));
        return user;
    }
}
