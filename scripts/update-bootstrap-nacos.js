/**
 * 将所有 bootstrap.yml 中的 Nacos 地址从 127.0.0.1:8848 改为 ruoyi-nacos:8848
 */
const fs = require('fs');
const path = require('path');

const modules = [
    'ruoyi-auth',
    'ruoyi-gateway',
    'ruoyi-visual/ruoyi-monitor',
    'ruoyi-modules/ruoyi-system',
    'ruoyi-modules/ruoyi-gen',
    'ruoyi-modules/ruoyi-job',
    'ruoyi-modules/ruoyi-file',
];

modules.forEach(mod => {
    const file = path.join(__dirname, '..', mod, 'src/main/resources/bootstrap.yml');
    if (fs.existsSync(file)) {
        let content = fs.readFileSync(file, 'utf-8');
        const before = content;
        // 将 bootstrap.yml 中所有 127.0.0.1:8848 替换为 ruoyi-nacos:8848（Nacos 地址）
        content = content.replace(/server-addr:\s*127\.0\.0\.1:8848/g, 'server-addr: ruoyi-nacos:8848');
        if (content !== before) {
            fs.writeFileSync(file, content, 'utf-8');
            console.log(`Updated: ${file}`);
        } else {
            console.log(`No change: ${file}`);
        }
    } else {
        console.warn(`Not found: ${file}`);
    }
});
