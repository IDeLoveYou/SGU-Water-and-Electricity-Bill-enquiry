package cn.rabig.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.rabig.main.MainClass;

public class LoginUtils {
    //记录session信息
    public static String JSESSIONID;

    //记录二维码key
    private static String qrCodeKey;

    //定义刷新二维码的次数
    private static int qrCodeCount = 1;

    //获取codeKey的请求头
    private static final HttpRequest qrCodeKeyRequest;

    //获取二维码状态的请求头
    private static final HttpRequest qrCodeStatusRequest;

    //获取校园卡中心的jSessionId请求头
    private static final HttpRequest jSessionIdRequest;

    //初始化请求头
    static {
        qrCodeKeyRequest = HttpRequest.get("https://login.work.weixin.qq.com/wwlogin/sso/login?login_type=CorpApp&appid=wx17acc31323b811e0&redirect_uri=http://wxcard.sgu.edu.cn/wechat/url/redirect.html?jkbh=0006&state=wechat_portal&agentid=1000021").keepAlive(true).timeout(10000);
        qrCodeStatusRequest = HttpRequest.post("https://login.work.weixin.qq.com/wwlogin/monoApi/sso/login/getWebQrCodeStatus?lang=zh_CN&ajax=1&f=json&random=" + (int) ((Math.random() * 9 + 1) * 100000)).header("Referer", "https://login.work.weixin.qq.com/").keepAlive(true).timeout(-1);
        jSessionIdRequest = HttpRequest.get("http://wxcard.sgu.edu.cn/wechat/url/redirect.html?jkbh=0006&state=wechat_portal").keepAlive(true).timeout(10000);
    }

    /**
     * 获取二维码key
     *
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/14 16:26
     */
    private static void getQrCodeKey() {
        try {
            String responseBody = qrCodeKeyRequest.execute().body();
            qrCodeKey = StrUtil.subBetween(responseBody, "key=", "\"");
        } catch (Exception e) {
            getQrCodeKey();
        }
    }

    /**
     * 获取二维码状态
     *
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/14 16:33
     */
    private static boolean getQrCodeStatus() {
        String body = qrCodeStatusRequest.body("{\"webKey\":\"" + qrCodeKey + "\"}").execute().body();
        //登录超时 || 拒绝登录
        if (body.contains("QRCODE_SCAN_ERR") || body.contains("QRCODE_SCAN_FAIL")) {
            return false;
        } else if (body.contains("QRCODE_SCAN_SUCC")) {
            //登录成功
            //解析临时code
            String authCode = StrUtil.subBetween(body, "\"auth_code\":\"", "\"");
            //解析校园卡中心响应头中的JSESSIONID
            String headers = jSessionIdRequest.body("code=" + authCode).execute().headers().toString();
            String jSessionId = StrUtil.subBetween(headers, "Set-Cookie=[", ";");
            if (jSessionId == null) {
                return false;
            } else {
                JSESSIONID = jSessionId;
                return true;
            }
        } else {
            CommonUtils.sleep(1000);
            return getQrCodeStatus();
        }
    }

    /**
     * 二维码登录函数
     *
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/14 16:27
     */
    public static void qrLogin() {
        if (qrCodeCount <= MainClass.verificationCount) {
            //获取二维码Key
            getQrCodeKey();
            //发送登录请求
            SendMess.sendAdminLoginRequest(qrCodeCount, "https://login.work.weixin.qq.com/wwlogin/sso/qrcode?key=" + qrCodeKey);
            //监控二维码状态
            if (!getQrCodeStatus()) {
                qrCodeCount++;
                qrLogin();
            } else {
                qrCodeCount = 0;
                //二维码登录成功
                SendMess.sendAdminInfo("二维码登录验证成功");
            }
        } else {
            SendMess.sendAdminError("登录验证超时，退出脚本");
            //登录超过次数，退出脚本
            CommonUtils.exit();
        }
    }
}
