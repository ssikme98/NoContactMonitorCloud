/**
 * 将 MyBatis Mapper XML 中的 MySQL 特有函数替换为 Kingbase/PostgreSQL 语法
 */
const fs = require('fs');
const path = require('path');

const mappers = [
    'ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/SysUserMapper.xml',
    'ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/SysRoleMapper.xml',
    'ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/SysDictTypeMapper.xml',
    'ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/SysDeptMapper.xml',
    'ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/SysMenuMapper.xml',
    'ruoyi-modules/ruoyi-system/src/main/resources/mapper/system/SysConfigMapper.xml',
    'ruoyi-modules/ruoyi-job/src/main/resources/mapper/job/SysJobLogMapper.xml',
    'ruoyi-modules/ruoyi-gen/src/main/resources/mapper/generator/GenTableMapper.xml',
];

mappers.forEach(file => {
    const fullPath = path.join(__dirname, '..', file);
    if (!fs.existsSync(fullPath)) {
        console.warn(`Not found: ${fullPath}`);
        return;
    }
    let content = fs.readFileSync(fullPath, 'utf-8');
    const before = content;

    // ifnull -> coalesce
    content = content.replace(/\bifnull\s*\(/gi, 'COALESCE(');

    // date_format(col, '%Y%m%d') -> TO_CHAR(col, 'YYYYMMDD')
    content = content.replace(/\bdate_format\s*\(\s*([^,]+?)\s*,\s*'(%Y%m%d)'\s*\)/gi, (m, col, fmt) => {
        return `TO_CHAR(${col}, 'YYYYMMDD')`;
    });

    // find_in_set(#{param}, column) -> 字符串包含判断
    // 例如 find_in_set(#{deptId}, ancestors) 替换为：
    // (',' || ancestors || ',' LIKE '%,' || #{deptId} || ',%')
    content = content.replace(/\bfind_in_set\s*\(\s*([^,]+?)\s*,\s*([^)]+?)\s*\)/gi, (m, param, col) => {
        return `',' || ${col} || ',' LIKE '%,' || ${param} || ',%'`;
    });

    // MySQL 反引号 -> 双引号（针对字段名）
    content = content.replace(/`(\w+)`/g, '"$1"');

    if (content !== before) {
        fs.writeFileSync(fullPath, content, 'utf-8');
        console.log(`Updated: ${file}`);
    } else {
        console.log(`No change: ${file}`);
    }
});
