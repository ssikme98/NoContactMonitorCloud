@echo off
chcp 65001 > nul
REM 安装人大金仓 Kingbase JDBC 驱动到本地 Maven 仓库
REM 用法：将 Kingbase 驱动 JAR 重命名为 kingbase8-8.6.0.jar 放在项目根目录，然后运行此脚本

set JAR_FILE=kingbase8-8.6.0.jar
set GROUP_ID=com.kingbase8
set ARTIFACT_ID=kingbase8
set VERSION=8.6.0

if not exist %JAR_FILE% (
    echo 错误：找不到 %JAR_FILE%
    echo 请从 Kingbase 安装目录或官网获取 JDBC 驱动 JAR，并放在项目根目录。
    exit /b 1
)

call mvn install:install-file -Dfile=%JAR_FILE% -DgroupId=%GROUP_ID% -DartifactId=%ARTIFACT_ID% -Dversion=%VERSION% -Dpackaging=jar

if %ERRORLEVEL% neq 0 (
    echo 安装失败
    exit /b %ERRORLEVEL%
)

echo Kingbase 驱动已安装到本地 Maven 仓库：%GROUP_ID%:%ARTIFACT_ID%:%VERSION%
