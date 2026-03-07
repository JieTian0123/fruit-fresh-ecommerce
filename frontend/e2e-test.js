import { chromium } from 'playwright';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

// 创建临时截图目录
const tmpDir = path.join(__dirname, 'tmp-screenshots');
if (!fs.existsSync(tmpDir)) {
  fs.mkdirSync(tmpDir, { recursive: true });
}

async function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function runTests() {
  let browser;
  try {
    browser = await chromium.launch({ headless: false });
    const page = await browser.newPage();
    
    console.log('========== 测试 1: 商家登出 - 仅显示一条 toast ==========');
    
    // 测试 1: 导航到登录页
    console.log('1.1 导航到商家登录页...');
    await page.goto('http://localhost:3000/merchant/login', { waitUntil: 'networkidle' });
    await delay(1000);
    
    // 填写登录表单
    console.log('1.2 填写登录表单 merchant1 / 123456...');
    await page.fill('input[placeholder*="用户名"], input[placeholder*="username"]', 'merchant1');
    await delay(300);
    await page.fill('input[placeholder*="密码"], input[placeholder*="password"]', '123456');
    await delay(300);
    
    // 点击登录按钮
    console.log('1.3 点击登录按钮...');
    await Promise.all([
      page.click('button:has-text("登录")'),
      page.waitForNavigation({ waitUntil: 'networkidle' })
    ]);
    await delay(2000);
    
    console.log('1.4 等待仪表板加载...');
    await page.waitForSelector('[class*="layout"], [class*="dashboard"], [class*="content"]', { timeout: 10000 });
    await delay(1000);
    
    // 查找并点击用户下拉菜单
    console.log('1.5 寻找用户下拉菜单...');
    const headerElements = await page.locator('header, [class*="header"]').all();
    if (headerElements.length > 0) {
      // 尝试在右上角找到用户菜单
      const rightArea = await page.locator('[class*="user"], [class*="avatar"], [class*="profile"], button').filter({ hasNot: page.locator('text=首页') }).all();
      if (rightArea.length > 0) {
        await rightArea[rightArea.length - 1].click();
        await delay(500);
      }
    }
    
    // 点击退出登录
    console.log('1.6 点击"退出登录"按钮...');
    try {
      await page.click('text=退出登录');
      console.log('1.6 成功点击退出登录');
    } catch (e) {
      console.log('1.6 未能找到退出按钮，尝试其他方式...');
      await page.click('div:has-text("退出登录"), button:has-text("退出登录"), a:has-text("退出登录")');
    }
    
    console.log('1.7 立即截图（检查是否只显示一条 toast）...');
    await delay(300);
    await page.screenshot({ path: path.join(tmpDir, '01_logout_first_toast.png') });
    await delay(2000);
    
    console.log('1.8 等待2秒后再次截图（检查是否有第二条 toast）...');
    await page.screenshot({ path: path.join(tmpDir, '02_logout_second_check.png') });
    
    console.log('\n========== 测试 2: 商家侧边栏固定位置 ==========');
    
    // 重新登录
    console.log('2.1 重新导航到商家登录页...');
    await page.goto('http://localhost:3000/merchant/login', { waitUntil: 'networkidle' });
    await delay(1000);
    
    console.log('2.2 登录...');
    await page.fill('input[placeholder*="用户名"], input[placeholder*="username"]', 'merchant1');
    await delay(300);
    await page.fill('input[placeholder*="密码"], input[placeholder*="password"]', '123456');
    await delay(300);
    
    await Promise.all([
      page.click('button:has-text("登录")'),
      page.waitForNavigation({ waitUntil: 'networkidle' })
    ]);
    await delay(2000);
    
    console.log('2.3 等待仪表板加载...');
    await page.waitForSelector('[class*="layout"], [class*="dashboard"]', { timeout: 10000 });
    await delay(1000);
    
    console.log('2.4 截图初始状态（侧边栏位置）...');
    await page.screenshot({ path: path.join(tmpDir, '03_merchant_sidebar_initial.png') });
    
    console.log('2.5 滚动页面内容...');
    await page.evaluate(() => {
      window.scrollBy(0, 500);
    });
    await delay(500);
    
    console.log('2.6 滚动后截图（侧边栏应该固定）...');
    await page.screenshot({ path: path.join(tmpDir, '04_merchant_sidebar_scrolled.png') });
    
    console.log('\n========== 测试 3: 商家注册页面 ==========');
    
    console.log('3.1 导航到商家注册页...');
    await page.goto('http://localhost:3000/merchant/register', { waitUntil: 'networkidle' });
    await delay(1500);
    
    console.log('3.2 截图注册页面...');
    await page.screenshot({ path: path.join(tmpDir, '05_merchant_register_page.png') });
    
    console.log('3.3 验证页面元素...');
    const pageContent = await page.content();
    const hasTitle = pageContent.includes('商家注册') || pageContent.includes('申请');
    const hasUsername = await page.locator('input[placeholder*="用户"]').count();
    const hasPhone = await page.locator('input[placeholder*="电话"], input[placeholder*="phone"]').count();
    const hasPassword = await page.locator('input[type="password"]').count();
    
    console.log(`  - 标题检查: ${hasTitle ? '✓' : '✗'}`);
    console.log(`  - 用户名输入框: ${hasUsername > 0 ? '✓' : '✗'}`);
    console.log(`  - 电话输入框: ${hasPhone > 0 ? '✓' : '✗'}`);
    console.log(`  - 密码输入框: ${hasPassword > 0 ? '✓' : '✗'}`);
    
    console.log('\n========== 测试 4: 管理员商家管理页面 ==========');
    
    console.log('4.1 导航到管理员登录页...');
    await page.goto('http://localhost:3000/admin/login', { waitUntil: 'networkidle' });
    await delay(1000);
    
    console.log('4.2 填写管理员登录表单...');
    await page.fill('input[placeholder*="用户名"], input[placeholder*="username"]', 'admin');
    await delay(300);
    await page.fill('input[placeholder*="密码"], input[placeholder*="password"]', 'admin123');
    await delay(300);
    
    console.log('4.3 点击登录...');
    await Promise.all([
      page.click('button:has-text("登录")'),
      page.waitForNavigation({ waitUntil: 'networkidle' })
    ]);
    await delay(2000);
    
    console.log('4.4 导航到商家管理页...');
    await page.goto('http://localhost:3000/admin/merchants', { waitUntil: 'networkidle' });
    await delay(2000);
    
    console.log('4.5 等待表格加载...');
    await page.waitForSelector('table, [class*="table"], [class*="merchants"], tr', { timeout: 10000 });
    await delay(1000);
    
    console.log('4.6 截图商家列表...');
    await page.screenshot({ path: path.join(tmpDir, '06_admin_merchants_list.png') });
    
    // 检查是否显示真实数据
    const tableContent = await page.content();
    const hasRealData = !tableContent.includes('新鲜果园') || tableContent.includes('merchant');
    console.log(`  - 显示真实数据: ${hasRealData ? '✓' : '✗'}`);
    
    console.log('\n========== 测试 5: 管理员侧边栏固定位置 ==========');
    
    console.log('5.1 滚动管理员页面...');
    await page.evaluate(() => {
      window.scrollBy(0, 500);
    });
    await delay(500);
    
    console.log('5.2 滚动后截图（侧边栏应该固定）...');
    await page.screenshot({ path: path.join(tmpDir, '07_admin_sidebar_scrolled.png') });
    
    console.log('\n========== 所有测试完成 ==========');
    console.log(`\n截图已保存到: ${tmpDir}`);
    console.log('文件列表:');
    const files = fs.readdirSync(tmpDir);
    files.forEach(file => console.log(`  - ${file}`));
    
  } catch (error) {
    console.error('测试失败:', error);
  } finally {
    if (browser) {
      await browser.close();
    }
  }
}

runTests();
