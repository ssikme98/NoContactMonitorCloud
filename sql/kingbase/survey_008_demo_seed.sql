-- 问卷模块 008：湖南营商环境拟真演示数据
-- 前置建表脚本：survey_002_enterprise.sql、survey_003_questionnaire.sql、survey_004_task.sql、survey_005_response.sql
-- 批次标识：DEMO-HN-20260624-V1
-- 说明：企业名称、联系人、手机号、统一社会信用代码均为演示样本，不对应真实企业或个人。

DO $$
DECLARE
  batch text := 'DEMO-HN-20260624-V1';
  seed_user text := 'demo-seed-008';
  group_id_value bigint;
  questionnaire_1_id bigint;
  questionnaire_2_id bigint;
  task_1_id bigint;
  task_2_id bigint;
  q1_single_id bigint;
  q1_multiple_id bigint;
  q1_score_id bigint;
  q1_likert_id bigint;
  q1_matrix_id bigint;
  q1_text_id bigint;
  q2_single_id bigint;
  q2_multiple_id bigint;
  q2_score_id bigint;
  q2_likert_id bigint;
  q2_matrix_id bigint;
  q2_text_id bigint;
BEGIN
  DELETE FROM survey_response_answer
   WHERE response_id IN (
     SELECT r.response_id
       FROM survey_response r
       LEFT JOIN survey_task t ON t.task_id = r.task_id
       LEFT JOIN survey_task_sample s ON s.sample_id = r.sample_id
      WHERE t.remark = batch OR s.token LIKE batch || '-%'
   );

  DELETE FROM survey_response
   WHERE task_id IN (SELECT task_id FROM survey_task WHERE remark = batch)
      OR sample_id IN (SELECT sample_id FROM survey_task_sample WHERE token LIKE batch || '-%');

  DELETE FROM survey_task_send_record
   WHERE task_id IN (SELECT task_id FROM survey_task WHERE remark = batch)
      OR sample_id IN (SELECT sample_id FROM survey_task_sample WHERE token LIKE batch || '-%');

  DELETE FROM survey_task_sample
   WHERE task_id IN (SELECT task_id FROM survey_task WHERE remark = batch)
      OR token LIKE batch || '-%';

  DELETE FROM survey_task WHERE remark = batch;

  DELETE FROM survey_question_option
   WHERE question_id IN (
     SELECT q.question_id
       FROM survey_question q
       JOIN survey_questionnaire n ON n.questionnaire_id = q.questionnaire_id
      WHERE n.remark = batch
   );

  DELETE FROM survey_question
   WHERE questionnaire_id IN (SELECT questionnaire_id FROM survey_questionnaire WHERE remark = batch);

  DELETE FROM survey_questionnaire WHERE remark = batch;

  DELETE FROM survey_enterprise_group_rel
   WHERE group_id IN (SELECT group_id FROM survey_enterprise_group WHERE remark = batch)
      OR enterprise_id IN (SELECT enterprise_id FROM survey_enterprise WHERE remark = batch);

  DELETE FROM survey_enterprise WHERE remark = batch;
  DELETE FROM survey_enterprise_group WHERE remark = batch;

  INSERT INTO survey_enterprise_group(
    parent_id, ancestors, group_name, order_num, status, del_flag,
    create_by, create_time, remark
  ) VALUES (
    0, '0', '湖南营商环境演示样本（' || batch || '）', 1, '0', '0',
    seed_user, current_timestamp, batch
  ) RETURNING group_id INTO group_id_value;

  INSERT INTO survey_enterprise(
    enterprise_name, credit_code, industry_category, region_code, region_name,
    enterprise_scale, contact_name, contact_phone, status, del_flag,
    create_by, create_time, remark
  )
  SELECT enterprise_name,
         '9143' || lpad(no_value::text, 4, '0') || 'MADEMO' || lpad(no_value::text, 4, '0'),
         industry_category,
         region_code,
         region_name,
         enterprise_scale,
         '企服联系人' || lpad(no_value::text, 2, '0'),
         '1990001' || lpad(no_value::text, 4, '0'),
         '0',
         '0',
         seed_user,
         current_timestamp,
         batch
    FROM (
      VALUES
        (1,  '湖南湘江智造样本科技有限公司',       '智能制造',       '430100', '长沙市',       '中型'),
        (2,  '长沙星城数链样本科技有限公司',       '软件和信息服务', '430100', '长沙市',       '小型'),
        (3,  '湖南岳麓新材样本有限公司',           '新材料',         '430100', '长沙市',       '中型'),
        (4,  '长沙雨花企服样本供应链有限公司',     '供应链服务',     '430100', '长沙市',       '小型'),
        (5,  '浏阳经开生物样本科技有限公司',       '生物医药',       '430100', '长沙市',       '中型'),
        (6,  '株洲云轨装备样本制造有限公司',       '装备制造',       '430200', '株洲市',       '中型'),
        (7,  '株洲天元精工样本有限公司',           '精密仪器',       '430200', '株洲市',       '小型'),
        (8,  '醴陵陶瓷新材样本有限公司',           '绿色建材',       '430200', '株洲市',       '小型'),
        (9,  '株洲渌口农机样本制造有限公司',       '装备制造',       '430200', '株洲市',       '微型'),
        (10, '湘潭雨湖机电样本有限公司',           '工程机械配套',   '430300', '湘潭市',       '中型'),
        (11, '湘潭九华电子样本科技有限公司',       '电子信息',       '430300', '湘潭市',       '小型'),
        (12, '湘乡绿色食品样本有限公司',           '食品加工',       '430300', '湘潭市',       '小型'),
        (13, '衡阳雁峰新能源样本有限公司',         '新能源',         '430400', '衡阳市',       '中型'),
        (14, '衡阳石鼓物流样本有限公司',           '现代物流',       '430400', '衡阳市',       '小型'),
        (15, '衡南油茶加工样本有限公司',           '农产品加工',     '430400', '衡阳市',       '微型'),
        (16, '岳阳洞庭港航样本服务有限公司',       '现代物流',       '430600', '岳阳市',       '中型'),
        (17, '岳阳楼区化工新材样本有限公司',       '新材料',         '430600', '岳阳市',       '小型'),
        (18, '汨罗循环经济样本有限公司',           '节能环保',       '430600', '岳阳市',       '小型'),
        (19, '常德柳叶湖文旅样本有限公司',         '文化旅游',       '430700', '常德市',       '小型'),
        (20, '常德武陵数字样本科技有限公司',       '软件和信息服务', '430700', '常德市',       '中型'),
        (21, '桃源农产品深加工样本有限公司',       '农产品加工',     '430700', '常德市',       '小型'),
        (22, '益阳高新区电子样本有限公司',         '电子信息',       '430900', '益阳市',       '中型'),
        (23, '南县稻虾加工样本有限公司',           '农产品加工',     '430900', '益阳市',       '微型'),
        (24, '沅江船舶配套样本有限公司',           '装备制造',       '430900', '益阳市',       '小型'),
        (25, '郴州高新有色新材样本有限公司',       '新材料',         '431000', '郴州市',       '中型'),
        (26, '郴州北湖跨境样本电商有限公司',       '跨境电商',       '431000', '郴州市',       '小型'),
        (27, '资兴文旅运营样本有限公司',           '文化旅游',       '431000', '郴州市',       '微型'),
        (28, '永州冷水滩农机样本有限公司',         '装备制造',       '431100', '永州市',       '小型'),
        (29, '祁阳纺织样本有限公司',               '轻工纺织',       '431100', '永州市',       '小型'),
        (30, '道县果蔬加工样本有限公司',           '农产品加工',     '431100', '永州市',       '微型'),
        (31, '怀化鹤城医药流通样本有限公司',       '生物医药',       '431200', '怀化市',       '小型'),
        (32, '怀化国际陆港样本物流有限公司',       '现代物流',       '431200', '怀化市',       '中型'),
        (33, '洪江竹木加工样本有限公司',           '农产品加工',     '431200', '怀化市',       '微型'),
        (34, '娄底经开钢构样本有限公司',           '工程机械配套',   '431300', '娄底市',       '中型'),
        (35, '娄星区工业设计样本有限公司',         '工业设计',       '431300', '娄底市',       '小型'),
        (36, '涟源新能源配套样本有限公司',         '新能源',         '431300', '娄底市',       '小型'),
        (37, '吉首数字文旅样本有限公司',           '文化旅游',       '433100', '湘西州',       '小型'),
        (38, '凤凰非遗产品样本有限公司',           '文化旅游',       '433100', '湘西州',       '微型'),
        (39, '龙山茶旅融合样本有限公司',           '现代农业',       '433100', '湘西州',       '微型'),
        (40, '张家界永定旅服样本有限公司',         '文化旅游',       '430800', '张家界市',     '小型'),
        (41, '慈利绿色建材样本有限公司',           '绿色建材',       '430800', '张家界市',     '微型'),
        (42, '邵阳双清装备样本有限公司',           '装备制造',       '430500', '邵阳市',       '小型'),
        (43, '邵东小商品样本供应链有限公司',       '供应链服务',     '430500', '邵阳市',       '中型'),
        (44, '洞口雪峰食品样本有限公司',           '食品加工',       '430500', '邵阳市',       '微型'),
        (45, '湘江新区人工智能样本有限公司',       '软件和信息服务', '430100', '长沙市',       '中型'),
        (46, '宁乡储能材料样本有限公司',           '新能源',         '430100', '长沙市',       '中型'),
        (47, '湘潭韶山研学样本服务有限公司',       '文化旅游',       '430300', '湘潭市',       '微型'),
        (48, '衡阳珠晖医疗器械样本有限公司',       '医疗器械',       '430400', '衡阳市',       '小型'),
        (49, '岳阳临港装备样本有限公司',           '装备制造',       '430600', '岳阳市',       '中型'),
        (50, '常德津市绿色包装样本有限公司',       '轻工制造',       '430700', '常德市',       '小型')
    ) AS enterprise_seed(no_value, enterprise_name, industry_category, region_code, region_name, enterprise_scale);

  INSERT INTO survey_enterprise_group_rel(enterprise_id, group_id, create_by, create_time)
  SELECT enterprise_id, group_id_value, seed_user, current_timestamp
    FROM survey_enterprise
   WHERE remark = batch;

  INSERT INTO survey_questionnaire(
    questionnaire_name, description, status, version_no, published_time,
    del_flag, create_by, create_time, remark
  ) VALUES (
    '湖南营商环境企业满意度调查（' || batch || '）',
    '面向湖南省拟真样本企业的营商环境满意度演示问卷',
    '1',
    1,
    current_timestamp - interval '10 days',
    '0',
    seed_user,
    current_timestamp,
    batch
  ) RETURNING questionnaire_id INTO questionnaire_1_id;

  INSERT INTO survey_questionnaire(
    questionnaire_name, description, status, version_no, published_time,
    del_flag, create_by, create_time, remark
  ) VALUES (
    '湖南产业链配套服务回访调查（' || batch || '）',
    '面向重点产业链配套服务的拟真演示问卷',
    '1',
    1,
    current_timestamp - interval '8 days',
    '0',
    seed_user,
    current_timestamp,
    batch
  ) RETURNING questionnaire_id INTO questionnaire_2_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_1_id, '贵企业所在园区服务响应速度如何？', 'single', '1', '政务服务', 0, 1, '0')
  RETURNING question_id INTO q1_single_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_1_id, '近一年企业主要享受过哪些涉企服务？', 'multiple', '1', '惠企政策', 0, 2, '0')
  RETURNING question_id INTO q1_multiple_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_1_id, '请为政务窗口办事便利度评分', 'score', '1', '政务服务', 10, 3, '0')
  RETURNING question_id INTO q1_score_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_1_id, '对惠企政策兑现及时性的认可程度', 'likert', '1', '惠企政策', 0, 4, '0')
  RETURNING question_id INTO q1_likert_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_1_id, '请评价以下营商环境要素', 'matrix_score', '1', '综合环境', 5, 5, '0')
  RETURNING question_id INTO q1_matrix_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_1_id, '请填写一条希望优化的涉企服务事项', 'text', '0', '开放建议', 0, 6, '0')
  RETURNING question_id INTO q1_text_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_2_id, '融资服务对企业扩产支持效果如何？', 'single', '1', '要素保障', 0, 1, '0')
  RETURNING question_id INTO q2_single_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_2_id, '当前最需要加强的要素保障有哪些？', 'multiple', '1', '要素保障', 0, 2, '0')
  RETURNING question_id INTO q2_multiple_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_2_id, '请为产业链协同服务评分', 'score', '1', '产业协同', 10, 3, '0')
  RETURNING question_id INTO q2_score_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_2_id, '对跨部门协同办事体验的认可程度', 'likert', '1', '政务协同', 0, 4, '0')
  RETURNING question_id INTO q2_likert_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_2_id, '请评价以下公共服务支撑', 'matrix_score', '1', '公共服务', 5, 5, '0')
  RETURNING question_id INTO q2_matrix_id;

  INSERT INTO survey_question(questionnaire_id, question_title, question_type, required_flag, dimension, score_max, order_num, del_flag)
  VALUES (questionnaire_2_id, '请描述产业链配套服务中最需要改进的一点', 'text', '0', '开放建议', 0, 6, '0')
  RETURNING question_id INTO q2_text_id;

  INSERT INTO survey_question_option(question_id, option_type, option_label, option_value, score_value, order_num)
  VALUES
    (q1_single_id, 'option', '响应很快', 'fast', NULL, 1),
    (q1_single_id, 'option', '基本及时', 'normal', NULL, 2),
    (q1_single_id, 'option', '仍需加快', 'slow', NULL, 3),
    (q1_multiple_id, 'option', '税费减免辅导', 'tax', NULL, 1),
    (q1_multiple_id, 'option', '融资对接', 'finance', NULL, 2),
    (q1_multiple_id, 'option', '人才服务', 'talent', NULL, 3),
    (q1_multiple_id, 'option', '审批帮代办', 'approval', NULL, 4),
    (q1_multiple_id, 'option', '法律咨询', 'legal', NULL, 5),
    (q1_likert_id, 'option', '非常不认可', 'strongly_disagree', 1, 1),
    (q1_likert_id, 'option', '不太认可', 'disagree', 2, 2),
    (q1_likert_id, 'option', '一般', 'neutral', 3, 3),
    (q1_likert_id, 'option', '比较认可', 'agree', 4, 4),
    (q1_likert_id, 'option', '非常认可', 'strongly_agree', 5, 5),
    (q1_matrix_id, 'row', '审批效率', 'approval', NULL, 1),
    (q1_matrix_id, 'row', '税务服务', 'tax', NULL, 2),
    (q1_matrix_id, 'row', '园区配套', 'infrastructure', NULL, 3),
    (q1_matrix_id, 'row', '公平竞争', 'market', NULL, 4),
    (q1_matrix_id, 'column', '1分', '1', 1, 1),
    (q1_matrix_id, 'column', '2分', '2', 2, 2),
    (q1_matrix_id, 'column', '3分', '3', 3, 3),
    (q1_matrix_id, 'column', '4分', '4', 4, 4),
    (q1_matrix_id, 'column', '5分', '5', 5, 5),
    (q2_single_id, 'option', '支持明显', 'high', NULL, 1),
    (q2_single_id, 'option', '有一定支持', 'medium', NULL, 2),
    (q2_single_id, 'option', '支持有限', 'low', NULL, 3),
    (q2_single_id, 'option', '暂未感受到', 'none', NULL, 4),
    (q2_multiple_id, 'option', '用地保障', 'land', NULL, 1),
    (q2_multiple_id, 'option', '用电用能', 'power', NULL, 2),
    (q2_multiple_id, 'option', '物流通道', 'logistics', NULL, 3),
    (q2_multiple_id, 'option', '用工招聘', 'employment', NULL, 4),
    (q2_multiple_id, 'option', '数字化改造', 'digital', NULL, 5),
    (q2_likert_id, 'option', '非常不认可', 'strongly_disagree', 1, 1),
    (q2_likert_id, 'option', '不太认可', 'disagree', 2, 2),
    (q2_likert_id, 'option', '一般', 'neutral', 3, 3),
    (q2_likert_id, 'option', '比较认可', 'agree', 4, 4),
    (q2_likert_id, 'option', '非常认可', 'strongly_agree', 5, 5),
    (q2_matrix_id, 'row', '融资撮合', 'finance', NULL, 1),
    (q2_matrix_id, 'row', '平台服务', 'platform', NULL, 2),
    (q2_matrix_id, 'row', '培训辅导', 'training', NULL, 3),
    (q2_matrix_id, 'row', '外贸服务', 'export', NULL, 4),
    (q2_matrix_id, 'column', '1分', '1', 1, 1),
    (q2_matrix_id, 'column', '2分', '2', 2, 2),
    (q2_matrix_id, 'column', '3分', '3', 3, 3),
    (q2_matrix_id, 'column', '4分', '4', 4, 4),
    (q2_matrix_id, 'column', '5分', '5', 5, 5);

  INSERT INTO survey_task(
    task_name, questionnaire_id, sample_source, sampling_method, sample_size,
    group_id, token_expire_hours, send_channels, status, dispatch_time,
    del_flag, create_by, create_time, remark
  ) VALUES (
    '2026年湖南营商环境企业满意度演示任务（' || batch || '）',
    questionnaire_1_id,
    'group',
    'stratified',
    40,
    group_id_value,
    720,
    'sms,site',
    '2',
    current_timestamp - interval '6 days',
    '0',
    seed_user,
    current_timestamp,
    batch
  ) RETURNING task_id INTO task_1_id;

  INSERT INTO survey_task(
    task_name, questionnaire_id, sample_source, sampling_method, sample_size,
    group_id, token_expire_hours, send_channels, status, dispatch_time,
    del_flag, create_by, create_time, remark
  ) VALUES (
    '2026年湖南产业链配套服务回访演示任务（' || batch || '）',
    questionnaire_2_id,
    'group',
    'specified',
    30,
    group_id_value,
    720,
    'sms,site',
    '2',
    current_timestamp - interval '5 days',
    '0',
    seed_user,
    current_timestamp,
    batch
  ) RETURNING task_id INTO task_2_id;

  INSERT INTO survey_task_sample(
    task_id, enterprise_id, enterprise_name, credit_code, region_name,
    industry_category, enterprise_scale, contact_phone, token, token_expire_time,
    qr_content, status, create_time
  )
  SELECT task_1_id,
         enterprise_id,
         enterprise_name,
         credit_code,
         region_name,
         industry_category,
         enterprise_scale,
         contact_phone,
         batch || '-T1-' || lpad(row_number() OVER (ORDER BY enterprise_id)::text, 3, '0'),
         current_timestamp + interval '30 days',
         '/survey/fill?token=' || batch || '-T1-' || lpad(row_number() OVER (ORDER BY enterprise_id)::text, 3, '0'),
         CASE WHEN row_number() OVER (ORDER BY enterprise_id) <= 35 THEN '2' ELSE '1' END,
         current_timestamp
    FROM survey_enterprise
   WHERE remark = batch
     AND right(credit_code, 4)::int <= 40
   ORDER BY enterprise_id;

  INSERT INTO survey_task_sample(
    task_id, enterprise_id, enterprise_name, credit_code, region_name,
    industry_category, enterprise_scale, contact_phone, token, token_expire_time,
    qr_content, status, create_time
  )
  SELECT task_2_id,
         enterprise_id,
         enterprise_name,
         credit_code,
         region_name,
         industry_category,
         enterprise_scale,
         contact_phone,
         batch || '-T2-' || lpad(row_number() OVER (ORDER BY enterprise_id)::text, 3, '0'),
         current_timestamp + interval '30 days',
         '/survey/fill?token=' || batch || '-T2-' || lpad(row_number() OVER (ORDER BY enterprise_id)::text, 3, '0'),
         CASE WHEN row_number() OVER (ORDER BY enterprise_id) <= 25 THEN '2' ELSE '1' END,
         current_timestamp
    FROM survey_enterprise
   WHERE remark = batch
     AND right(credit_code, 4)::int BETWEEN 21 AND 50
   ORDER BY enterprise_id;

  INSERT INTO survey_task_send_record(
    task_id, sample_id, enterprise_id, channel, receiver, content, send_status, create_time
  )
  SELECT s.task_id,
         s.sample_id,
         s.enterprise_id,
         channel_seed.channel_name,
         CASE WHEN channel_seed.channel_name = 'sms' THEN s.contact_phone ELSE s.enterprise_name END,
         '【无感监测】请通过 ' || s.qr_content || ' 填报营商环境问卷，批次：' || batch,
         '0',
         s.create_time + interval '15 minutes'
    FROM survey_task_sample s
    CROSS JOIN (VALUES ('sms'), ('site')) AS channel_seed(channel_name)
   WHERE s.token LIKE batch || '-%';

  INSERT INTO survey_response(
    task_id, sample_id, enterprise_id, questionnaire_id, submit_time,
    client_ip, status, create_time
  )
  SELECT s.task_id,
         s.sample_id,
         s.enterprise_id,
         questionnaire_1_id,
         current_timestamp - ((36 - right(s.token, 3)::int) * interval '3 hours'),
         '10.8.1.' || ((right(s.token, 3)::int % 200) + 20),
         '0',
         current_timestamp - ((36 - right(s.token, 3)::int) * interval '3 hours')
    FROM survey_task_sample s
   WHERE s.task_id = task_1_id
     AND right(s.token, 3)::int <= 35;

  INSERT INTO survey_response(
    task_id, sample_id, enterprise_id, questionnaire_id, submit_time,
    client_ip, status, create_time
  )
  SELECT s.task_id,
         s.sample_id,
         s.enterprise_id,
         questionnaire_2_id,
         current_timestamp - ((26 - right(s.token, 3)::int) * interval '4 hours'),
         '10.8.2.' || ((right(s.token, 3)::int % 200) + 30),
         '0',
         current_timestamp - ((26 - right(s.token, 3)::int) * interval '4 hours')
    FROM survey_task_sample s
   WHERE s.task_id = task_2_id
     AND right(s.token, 3)::int <= 25;

  WITH response_rows AS (
    SELECT r.response_id, right(s.token, 3)::int AS seq_no
      FROM survey_response r
      JOIN survey_task_sample s ON s.sample_id = r.sample_id
     WHERE r.task_id = task_1_id
  )
  INSERT INTO survey_response_answer(response_id, question_id, question_type, answer_text, option_value, score_value)
  SELECT response_id, q1_single_id, 'single', '', CASE WHEN seq_no % 10 IN (0, 1) THEN 'slow' WHEN seq_no % 3 = 0 THEN 'normal' ELSE 'fast' END, NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q1_multiple_id, 'multiple',
         CASE seq_no % 5
           WHEN 0 THEN 'tax,approval'
           WHEN 1 THEN 'finance,talent'
           WHEN 2 THEN 'tax,legal'
           WHEN 3 THEN 'approval,finance,talent'
           ELSE 'tax,finance,approval'
         END,
         '', NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q1_score_id, 'score', '', '', CASE WHEN seq_no % 11 = 0 THEN 5 ELSE 6 + (seq_no % 5) END
    FROM response_rows
  UNION ALL
  SELECT response_id, q1_likert_id, 'likert', '',
         CASE WHEN seq_no % 9 = 0 THEN 'disagree' WHEN seq_no % 5 = 0 THEN 'neutral' WHEN seq_no % 2 = 0 THEN 'agree' ELSE 'strongly_agree' END,
         NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q1_matrix_id, 'matrix_score',
         '{"approval":"' || (CASE WHEN seq_no % 9 = 0 THEN 2 ELSE 3 + (seq_no % 3) END)::text ||
         '","tax":"' || (CASE WHEN seq_no % 7 = 0 THEN 3 ELSE 4 + (seq_no % 2) END)::text ||
         '","infrastructure":"' || (CASE WHEN seq_no % 6 = 0 THEN 2 ELSE 3 + (seq_no % 3) END)::text ||
         '","market":"' || (CASE WHEN seq_no % 8 = 0 THEN 3 ELSE 4 + (seq_no % 2) END)::text || '"}',
         '', NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q1_text_id, 'text',
         CASE seq_no % 4
           WHEN 0 THEN '建议进一步压缩跨部门材料重复提交次数。'
           WHEN 1 THEN '希望惠企政策申报进度能够在线实时查询。'
           WHEN 2 THEN '园区企业用工招聘服务还可以更精准。'
           ELSE '建议增加中小企业融资对接后的跟踪反馈。'
         END,
         '', NULL
    FROM response_rows;

  WITH response_rows AS (
    SELECT r.response_id, right(s.token, 3)::int AS seq_no
      FROM survey_response r
      JOIN survey_task_sample s ON s.sample_id = r.sample_id
     WHERE r.task_id = task_2_id
  )
  INSERT INTO survey_response_answer(response_id, question_id, question_type, answer_text, option_value, score_value)
  SELECT response_id, q2_single_id, 'single', '', CASE WHEN seq_no % 12 = 0 THEN 'none' WHEN seq_no % 5 = 0 THEN 'low' WHEN seq_no % 3 = 0 THEN 'medium' ELSE 'high' END, NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q2_multiple_id, 'multiple',
         CASE seq_no % 5
           WHEN 0 THEN 'land,power'
           WHEN 1 THEN 'logistics,employment'
           WHEN 2 THEN 'digital,employment'
           WHEN 3 THEN 'power,logistics,digital'
           ELSE 'land,employment,digital'
         END,
         '', NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q2_score_id, 'score', '', '', CASE WHEN seq_no % 10 = 0 THEN 5 ELSE 6 + ((seq_no + 2) % 5) END
    FROM response_rows
  UNION ALL
  SELECT response_id, q2_likert_id, 'likert', '',
         CASE WHEN seq_no % 8 = 0 THEN 'disagree' WHEN seq_no % 4 = 0 THEN 'neutral' WHEN seq_no % 2 = 0 THEN 'agree' ELSE 'strongly_agree' END,
         NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q2_matrix_id, 'matrix_score',
         '{"finance":"' || (CASE WHEN seq_no % 7 = 0 THEN 2 ELSE 3 + (seq_no % 3) END)::text ||
         '","platform":"' || (CASE WHEN seq_no % 5 = 0 THEN 3 ELSE 4 + (seq_no % 2) END)::text ||
         '","training":"' || (CASE WHEN seq_no % 6 = 0 THEN 2 ELSE 3 + ((seq_no + 1) % 3) END)::text ||
         '","export":"' || (CASE WHEN seq_no % 9 = 0 THEN 3 ELSE 4 + (seq_no % 2) END)::text || '"}',
         '', NULL
    FROM response_rows
  UNION ALL
  SELECT response_id, q2_text_id, 'text',
         CASE seq_no % 4
           WHEN 0 THEN '希望产业链供需对接活动形成长期机制。'
           WHEN 1 THEN '建议对小微企业数字化改造提供分阶段辅导。'
           WHEN 2 THEN '物流补贴和用能协调信息需要更透明。'
           ELSE '跨部门事项希望由一个窗口持续跟踪闭环。'
         END,
         '', NULL
    FROM response_rows;
END $$;
