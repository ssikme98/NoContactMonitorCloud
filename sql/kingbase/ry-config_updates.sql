-- 更新 Nacos 配置中心的 MySQL 连接为 Kingbase
UPDATE "config_info" SET "content" = REPLACE("content", 'com.mysql.cj.jdbc.Driver', 'com.kingbase8.Driver') WHERE "content" LIKE '%com.mysql.cj.jdbc.Driver%';
UPDATE "config_info" SET "content" = REPLACE("content", 'jdbc:mysql://localhost:3306/ry-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8', 'jdbc:kingbase8://121.40.112.55:54321/ry-cloud') WHERE "content" LIKE '%jdbc:mysql://localhost:3306/ry-cloud%';
UPDATE "config_info" SET "content" = REPLACE("content", 'jdbc:mysql://localhost:3306/ry-config?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC', 'jdbc:kingbase8://121.40.112.55:54321/ry-config') WHERE "content" LIKE '%jdbc:mysql://localhost:3306/ry-config%';
UPDATE "config_info" SET "content" = REPLACE("content", 'SELECT 1 FROM DUAL', 'SELECT 1') WHERE "content" LIKE '%SELECT 1 FROM DUAL%';
UPDATE "config_info" SET "content" = REPLACE("content", 'helperDialect: mysql', 'helperDialect: postgresql') WHERE "content" LIKE '%helperDialect: mysql%';
UPDATE "config_info" SET "content" = REPLACE("content", 'password: password', 'password: HGZJ2026') WHERE "content" LIKE '%password: password%';

-- 更新文件上传路径为 Linux/Docker 路径
UPDATE "config_info" SET "content" = REPLACE("content", 'path: D:/ruoyi/uploadPath', 'path: /home/ruoyi/uploadPath') WHERE "content" LIKE '%path: D:/ruoyi/uploadPath%';

-- 在 application-dev.yml 中增加 PageHelper 方言（如尚未配置）
UPDATE "config_info" SET "content" = CONCAT("content", E'\n# pagehelper 分页插件\npagehelper:\n  helperDialect: postgresql\n  offset-as-page-num: true\n  row-bounds-with-count: true\n  reasonable: false\n') WHERE "data_id" = 'application-dev.yml' AND "content" NOT LIKE '%pagehelper:%';
