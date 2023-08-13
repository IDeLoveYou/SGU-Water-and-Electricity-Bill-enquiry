package cn.rabig.utils;

import cn.hutool.core.util.StrUtil;
import cn.rabig.main.MainClass;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitForSelectorState;

public class LoginUtils {
    //记录session信息
    public static String JSESSIONID;

    //定义刷新二维码的次数
    private static int qrCodeCount = 1;

    private static final Browser.NewContextOptions newContextOptions;

    static {
        //仿真苹果12 pro的微企业微信内置浏览器
        newContextOptions = new Browser.NewContextOptions();
        newContextOptions.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 16_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 MicroMessenger/8.0.39(0x1800272d) NetType/WIFI Language/zh_CN");
        newContextOptions.setScreenSize(390, 844);
        newContextOptions.setViewportSize(390, 844);
        newContextOptions.setDeviceScaleFactor(3);
        newContextOptions.setIsMobile(true);
        newContextOptions.setHasTouch(true);
    }

    /**
     * 二维码过期刷新
     *
     * @param page [com.microsoft.playwright.Page]
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/13 3:43
     */
    private static void reFreshQrCode(Page page) {
        if (qrCodeCount <= MainClass.verificationCount) {
            //等待出现刷新二维码按钮
            page.waitForSelector("xpath=/html/body/div/div/div[1]/div/div[1]/section/div/div/div/a", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(0));
            //点击刷新
            page.querySelector("xpath=/html/body/div/div/div[1]/div/div[1]/section/div/div/div/a").click();
            //刷新二维码数递增
            qrCodeCount++;
            //刷新后重新等待二维码过期
            reFreshQrCode(page);
        } else {
            //为防止骚扰，程序发送既定数量的登录请求后，用户还未登录，即退出脚本
            CommonUtils.exit();
        }
    }

    /**
     * 通过企业微信二维码登录
     *
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/12 21:27
     */
    public static void qrLogin() {
        //重置刷新二维码数
        qrCodeCount = 1;
        //初始化并启动浏览器
        try (
                Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))
        ) {
            Page page = browser.newContext(newContextOptions).newPage();
            //创建一个请求拦截器，当有二维码时就发送给管理员
            page.onRequest(request -> {
                //获取到登录二维码
                if (request.url().contains("https://login.work.weixin.qq.com/wwlogin/sso/qrcode")) {
                    //发送登录请求
                    SendMess.sendAdminLoginRequest(qrCodeCount, request.url());
                }
            });
            page.onResponse(response -> {
                //登录成功
                if (response.url().contains("http://wxcard.sgu.edu.cn/wechat/basicQuery/getCardInfo.html") && response.status() == 200) {
                    JSESSIONID = response.request().headerValue("cookie");
                    String name = StrUtil.subBetween(response.text(), "\"name\":\"", "\",");
                    SendMess.sendAdminInfo("二维码登录验证成功，您好，" + name);
                    page.close();
                    browser.close();
                }
            });
            //跳转到登录界面
            page.navigate("https://login.work.weixin.qq.com/wwlogin/sso/login?login_type=CorpApp&appid=wx17acc31323b811e0&redirect_uri=http://wxcard.sgu.edu.cn/wechat/url/redirect.html?jkbh=0006&state=wechat_portal&agentid=1000021");
            reFreshQrCode(page);
        } catch (PlaywrightException ignored) {
            //忽略page在waitForSelector时强制关闭page和的browser错误
        }
    }
}
