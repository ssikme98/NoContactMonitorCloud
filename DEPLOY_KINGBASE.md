# RuoYi-Cloud + 人大金仓 Kingbase 容器化部署指南

## 一、前置条件

1. 本地已安装 JDK 8、Maven 3、Node.js 14+。
2. 已获取人大金仓 Kingbase V8 的 JDBC 驱动 JAR（通常名为 `kingbase8-xxx.jar`）。
3. 远程服务器已安装 JDK 8、Maven 3，且可访问 Kingbase 数据库（121.40.112.55:54321）。
4. 远程服务器可访问互联网以下载 Docker 镜像，或已准备好离线镜像包。

## 二、本地改造说明

本次改造已完成以下事项：

- `docker/docker-compose.yml`：移除 MySQL 服务，保留 Redis、Nacos、Nginx、业务服务。
- `docker/deploy.sh` / `docker/copy.sh`：移除 MySQL 相关逻辑。
- `docker/nacos/conf/application.properties`：改为连接 Kingbase 的 `ry-config` 库。
- `docker/nacos/dockerfile`：支持将 Kingbase 驱动复制到 Nacos 插件目录。
- `pom.xml` 及 `ruoyi-common/ruoyi-common-datasource/pom.xml`：添加 Kingbase 驱动依赖。
- `ruoyi-modules/*/pom.xml`：移除 MySQL 驱动，统一从 `ruoyi-common-datasource` 获取数据库驱动。
- 所有 `bootstrap.yml`：Nacos 地址从 `127.0.0.1:8848` 改为 `ruoyi-nacos:8848`（Docker 内部服务名）。
- `sql/kingbase/*.sql`：已转换为 Kingbase/PostgreSQL 兼容脚本。
- `sql/kingbase/ry-config_updates.sql`：导入 ry-config 后执行，将 Nacos 配置中的 MySQL 连接改为 Kingbase。
- MyBatis Mapper XML：已替换 `IFNULL`、`DATE_FORMAT`、`FIND_IN_SET` 等 MySQL 函数。

## 三、本地构建步骤

### 1. 安装 Kingbase 驱动到本地 Maven 仓库

将获取到的 Kingbase JDBC 驱动 JAR 重命名为 `kingbase8-8.6.0.jar`，放在项目根目录，然后执行：

```bash
# Windows
scripts/install-kingbase-driver.bat

# Linux / Git Bash
sh scripts/install-kingbase-driver.sh
```

> 注意：驱动版本号需与 `pom.xml` 中 `<kingbase8.version>8.6.0</kingbase8.version>` 保持一致。如版本不同，请同步修改 pom.xml 和脚本。

### 2. 前端构建

```bash
cd ruoyi-ui
npm install
npm run build:prod
```

构建完成后生成 `ruoyi-ui/dist/`。

### 3. Maven 打包

```bash
mvn clean package -Dmaven.test.skip=true
```

### 4. 复制构建产物到 Docker 目录

```bash
cd docker
sh copy.sh
```

### 5. 复制 Kingbase 驱动到 Nacos 构建目录

```bash
cp /path/to/kingbase8-8.6.0.jar docker/nacos/
```

### 6. 构建 Docker 镜像

```bash
cd docker
docker-compose build
```

### 7. 导出镜像（离线部署时使用）

```bash
docker save -o ruoyi-cloud-images.tar \
  ruoyi-gateway ruoyi-auth ruoyi-modules-system \
  ruoyi-modules-gen ruoyi-modules-job ruoyi-modules-file \
  ruoyi-visual-monitor ruoyi-nginx ruoyi-redis ruoyi-nacos
```

## 四、远程服务器部署步骤

### 1. 安装 Docker 与 Docker Compose

```bash
# CentOS / RHEL
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable --now docker

# Ubuntu
sudo apt update
sudo apt install -y ca-certificates curl gnupg lsb-release
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl enable --now docker
```

### 2. 开放防火墙端口

```bash
sudo firewall-cmd --add-port=80/tcp --permanent
sudo firewall-cmd --add-port=8080/tcp --permanent
sudo firewall-cmd --add-port=8848/tcp --permanent
sudo firewall-cmd --add-port=9848/tcp --permanent
sudo firewall-cmd --add-port=9849/tcp --permanent
sudo firewall-cmd --add-port=6379/tcp --permanent
sudo firewall-cmd --add-port=9100/tcp --permanent
sudo firewall-cmd --add-port=9200/tcp --permanent
sudo firewall-cmd --add-port=9201/tcp --permanent
sudo firewall-cmd --add-port=9202/tcp --permanent
sudo firewall-cmd --add-port=9203/tcp --permanent
sudo firewall-cmd --add-port=9300/tcp --permanent
sudo firewall-cmd --reload
```

### 3. 准备 Kingbase 数据库

登录 121.40.112.55 上的 Kingbase，执行：

```sql
CREATE DATABASE "ry-cloud";
CREATE DATABASE "ry-config";
CREATE DATABASE "ry-seata";
```

然后依次执行转换后的 SQL 脚本：

```bash
# 连接 Kingbase，注意替换为实际的用户名、密码、端口
ksql -h 121.40.112.55 -p 54321 -U root -d ry-config -f ry-config.sql
ksql -h 121.40.112.55 -p 54321 -U root -d ry-cloud -f ry-cloud.sql
ksql -h 121.40.112.55 -p 54321 -U root -d ry-seata -f ry-seata.sql
```

> `quartz.sql` 已合并到 `ry-cloud.sql` 中，无需单独执行。

### 4. 更新 Nacos 配置中心的连接信息

进入 `ry-config` 库，执行：

```bash
ksql -h 121.40.112.55 -p 54321 -U root -d ry-config -f ry-config_updates.sql
```

此脚本会：
- 将配置中的 MySQL 驱动改为 Kingbase 驱动
- 将 JDBC URL 改为 `jdbc:kingbase8://121.40.112.55:54321/...`
- 将密码改为 `HGZJ2026`
- 将 Redis 地址改为 `ruoyi-redis`
- 添加 PageHelper 的 `helperDialect: postgresql`

### 5. 上传部署包到服务器

将以下文件上传到远程服务器 `/opt/ruoyi-cloud/`：

```
docker/
├── docker-compose.yml
├── deploy.sh
├── copy.sh
├── nginx/
├── redis/
├── nacos/
│   ├── dockerfile
│   ├── conf/application.properties
│   └── kingbase8-8.6.0.jar   # Kingbase 驱动
├── ruoyi/
│   ├── gateway/jar/ruoyi-gateway.jar
│   ├── auth/jar/ruoyi-auth.jar
│   ├── modules/system/jar/ruoyi-modules-system.jar
│   ├── modules/gen/jar/ruoyi-modules-gen.jar
│   ├── modules/job/jar/ruoyi-modules-job.jar
│   ├── modules/file/jar/ruoyi-modules-file.jar
│   └── visual/monitor/jar/ruoyi-visual-monitor.jar
```

如果使用离线镜像，还需上传 `ruoyi-cloud-images.tar`。

### 6. 加载镜像并启动

```bash
cd /opt/ruoyi-cloud

# 离线部署时加载镜像
docker load -i ruoyi-cloud-images.tar

# 开放端口
sh deploy.sh port

# 启动基础环境（redis + nacos）
sh deploy.sh base

# 等待 Nacos 完全启动（约 30-60 秒）
docker-compose logs -f ruoyi-nacos

# 启动业务服务
sh deploy.sh modules
```

## 五、验证

1. 访问 Nacos 控制台：`http://<服务器IP>:8848/nacos`
2. 访问前端页面：`http://<服务器IP>`，默认账号 `admin/admin123`
3. 检查各服务日志：
   ```bash
   docker-compose logs -f ruoyi-modules-system
   docker-compose logs -f ruoyi-gateway
   ```

## 六、常见问题

### 1. Kingbase 驱动找不到

错误：`ClassNotFoundException: com.kingbase8.Driver`

解决：
- 确认已执行 `scripts/install-kingbase-driver.bat/sh`
- 确认 `pom.xml` 中的 `<kingbase8.version>` 与 JAR 版本一致
- 确认 `docker/nacos/kingbase8-*.jar` 存在

### 2. Nacos 无法连接 Kingbase

错误：`No suitable driver found for jdbc:kingbase8://...`

解决：
- 确认 Kingbase 驱动已放入 `docker/nacos/`
- 确认 `docker/nacos/dockerfile` 正确复制了驱动到 `/home/nacos/plugins/`
- 重新构建 Nacos 镜像：`docker-compose build ruoyi-nacos`

### 3. MyBatis SQL 报错

错误：`function ifnull/date_format/find_in_set does not exist`

解决：
- 确认已执行本改造中的 Mapper XML 替换脚本
- 确认 Nacos 配置中 `helperDialect` 已改为 `postgresql`

### 4. 服务注册不上 Nacos

错误：`failed to req API: /nacos/v1/ns/instance`

解决：
- 确认 `bootstrap.yml` 中 `server-addr` 为 `ruoyi-nacos:8848`
- 确认 Nacos 容器已完全启动
- 确认服务容器与 Nacos 容器在同一 Docker 网络

## 七、回滚

如需回滚到 MySQL：

1. 恢复 `docker/docker-compose.yml`、`docker/deploy.sh`、`docker/copy.sh` 的原始版本。
2. 恢复 `bootstrap.yml` 中的 Nacos 地址为 `127.0.0.1:8848`。
3. 恢复 `pom.xml` 中的 MySQL 驱动依赖。
4. 恢复原始的 `sql/` 脚本。
