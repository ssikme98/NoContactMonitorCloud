-- 问卷模块本地 MySQL 菜单同步脚本
-- 用途：当前若依本地/Docker system 服务仍使用 MySQL 时，同步问卷后台菜单。
-- 注意：问卷业务表和演示数据仍以 sql/kingbase 下脚本为准。

SET NAMES utf8mb4;

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES
(2000, '问卷调研', 0, 5, 'survey', NULL, '', '', 1, 0, 'M', '0', '0', '', 'form', 'admin', NOW(), '', NULL, '问卷调研目录'),
(2001, '企业库管理', 2000, 1, 'enterprise', 'survey/enterprise/index', '', '', 1, 0, 'C', '0', '0', 'survey:enterprise:list', 'peoples', 'admin', NOW(), '', NULL, '企业库管理菜单'),
(2002, '企业查询', 2001, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:query', '#', 'admin', NOW(), '', NULL, ''),
(2003, '企业新增', 2001, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:add', '#', 'admin', NOW(), '', NULL, ''),
(2004, '企业修改', 2001, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:edit', '#', 'admin', NOW(), '', NULL, ''),
(2005, '企业删除', 2001, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:remove', '#', 'admin', NOW(), '', NULL, ''),
(2006, '企业导出', 2001, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:export', '#', 'admin', NOW(), '', NULL, ''),
(2007, '企业导入', 2001, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:import', '#', 'admin', NOW(), '', NULL, ''),
(2008, '分组查询', 2001, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:group:list', '#', 'admin', NOW(), '', NULL, ''),
(2009, '分组详情', 2001, 8, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:group:query', '#', 'admin', NOW(), '', NULL, ''),
(2010, '分组新增', 2001, 9, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:group:add', '#', 'admin', NOW(), '', NULL, ''),
(2011, '分组修改', 2001, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:group:edit', '#', 'admin', NOW(), '', NULL, ''),
(2012, '分组删除', 2001, 11, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:enterprise:group:remove', '#', 'admin', NOW(), '', NULL, ''),
(2020, '问卷管理', 2000, 2, 'questionnaire', 'survey/questionnaire/index', '', '', 1, 0, 'C', '0', '0', 'survey:questionnaire:list', 'form', 'admin', NOW(), '', NULL, '问卷管理菜单'),
(2021, '问卷查询', 2020, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:questionnaire:query', '#', 'admin', NOW(), '', NULL, ''),
(2022, '问卷新增', 2020, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:questionnaire:add', '#', 'admin', NOW(), '', NULL, ''),
(2023, '问卷修改', 2020, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:questionnaire:edit', '#', 'admin', NOW(), '', NULL, ''),
(2024, '问卷删除', 2020, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:questionnaire:remove', '#', 'admin', NOW(), '', NULL, ''),
(2025, '问卷发布', 2020, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:questionnaire:publish', '#', 'admin', NOW(), '', NULL, ''),
(2026, '问卷结束', 2020, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:questionnaire:end', '#', 'admin', NOW(), '', NULL, ''),
(2027, '问卷列表', 2020, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:questionnaire:list', '#', 'admin', NOW(), '', NULL, ''),
(2030, '调研任务', 2000, 3, 'task', 'survey/task/index', '', '', 1, 0, 'C', '0', '0', 'survey:task:list', 'list', 'admin', NOW(), '', NULL, '调研任务菜单'),
(2031, '任务查询', 2030, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:task:query', '#', 'admin', NOW(), '', NULL, ''),
(2032, '任务新增', 2030, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:task:add', '#', 'admin', NOW(), '', NULL, ''),
(2033, '任务删除', 2030, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:task:remove', '#', 'admin', NOW(), '', NULL, ''),
(2034, '任务发卷', 2030, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:task:dispatch', '#', 'admin', NOW(), '', NULL, ''),
(2035, '任务列表', 2030, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'survey:task:list', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name),
  parent_id = VALUES(parent_id),
  order_num = VALUES(order_num),
  path = VALUES(path),
  component = VALUES(component),
  query = VALUES(query),
  route_name = VALUES(route_name),
  is_frame = VALUES(is_frame),
  is_cache = VALUES(is_cache),
  menu_type = VALUES(menu_type),
  visible = VALUES(visible),
  status = VALUES(status),
  perms = VALUES(perms),
  icon = VALUES(icon),
  update_by = 'admin',
  update_time = NOW(),
  remark = VALUES(remark);
