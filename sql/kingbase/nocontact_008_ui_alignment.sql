-- 营商无感业务模块 008：前端菜单命名与技术入口收敛

UPDATE sys_menu
SET menu_name = '营商数据融合',
    remark = '营商数据融合',
    update_by = 'admin',
    update_time = current_timestamp
WHERE menu_id = 3000;

UPDATE sys_menu
SET visible = '1',
    remark = '业务侧统一从采集计划管理进入',
    update_by = 'admin',
    update_time = current_timestamp
WHERE menu_id = 110;
