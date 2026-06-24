#!/bin/sh
# 安装人大金仓 Kingbase JDBC 驱动到本地 Maven 仓库
# 用法：将 Kingbase 驱动 JAR 重命名为 kingbase8-8.6.0.jar 放在项目根目录，然后运行此脚本

JAR_FILE="kingbase8-8.6.0.jar"
GROUP_ID="com.kingbase8"
ARTIFACT_ID="kingbase8"
VERSION="8.6.0"

if [ ! -f "$JAR_FILE" ]; then
    echo "错误：找不到 $JAR_FILE"
    echo "请从 Kingbase 安装目录或官网获取 JDBC 驱动 JAR，并放在项目根目录。"
    exit 1
fi

mvn install:install-file -Dfile="$JAR_FILE" -DgroupId="$GROUP_ID" -DartifactId="$ARTIFACT_ID" -Dversion="$VERSION" -Dpackaging=jar

if [ $? -ne 0 ]; then
    echo "安装失败"
    exit 1
fi

echo "Kingbase 驱动已安装到本地 Maven 仓库：$GROUP_ID:$ARTIFACT_ID:$VERSION"
