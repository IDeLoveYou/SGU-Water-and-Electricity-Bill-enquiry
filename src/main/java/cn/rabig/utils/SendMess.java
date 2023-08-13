package cn.rabig.utils;

import cn.hutool.http.HttpRequest;
import cn.rabig.main.MainClass;

public class SendMess {
    public static final String upInfo = "\n\n" +
            """
            ||更新内容(v""" + MainClass.version + """
            )|
            |-|-|
            |1|增加异常捕获，完善掉线重连机制|
            |2|新增充值提醒，该功能会有延迟|
            |3|删除sessionId保活进程|
            |4|优化对象复用，提高性能|
            |5|增加日志时间|
            |6|改用ini作为配置文件|
            |7|增加参数检测功能|
            |8|可自定义低于多少额度提醒|
            |9|修复参数检测一些bug|
            |10|修复息知信息漏发|
            |11|添加二维码登录|
            > 愿你在韶院发光发亮
            > version:""" + MainClass.version;

    public static final String adminInfo = "\n\n" +
            """
            |代号|错误提示|
            |-|-|
            |0x001|CommonUtils.exit()函数出现错误|
            |1x001|SendMess.sendAdminInfo()息知管理员信息发送失败|
            |1x002|SendMess.sendAdminError()息知用户信息发送失败|
            |1x003|SendMess.sendUserInfo()息知用户信息发送失败|
            |1x004|SendMess.sendAdminLoginRequest()息知用户信息发送失败|
            > 管理员提示
            > version:""" + MainClass.version;

    public static HttpRequest sendAdmin;
    public static HttpRequest sendUser;

    static {
        sendAdmin = HttpRequest.post(MainClass.adminUrl).keepAlive(true).timeout(20000);
        sendUser = HttpRequest.post(MainClass.userUrl).keepAlive(true).timeout(20000);
    }

    /**
     * 发送管理员信息
     *
     * @param isError [boolean]
     * @param title   [java.lang.String]
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/13 2:04
     */
    public static void sendAdmin(boolean isError, String title) {
        try {
            if (isError) {
                CommonUtils.error(title);
            } else {
                CommonUtils.log(title);
            }
            CommonUtils.sleep(1000);//间歇发送息知，避免频繁发送提示导致消息漏发
            //请求息知
            sendAdmin.body("title=" + title + "&content=### 欢迎使用韶关学院水电费提醒脚本" + adminInfo).execute();
        } catch (Exception e) {
            CommonUtils.log("息知发送失败(代号:1x001)，错误信息：" + e);
        }
    }

    /**
     * 发送管理员正确信息
     *
     * @param info [java.lang.String]
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/13 2:04
     */
    public static void sendAdminInfo(String info) {
        sendAdmin(false, info);
    }

    /**
     * 发送管理员错误信息
     *
     * @param error [java.lang.String]
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/13 2:03
     */
    public static void sendAdminError(String error) {
        sendAdmin(true, error);
    }

    /**
     * 发送用户信息
     *
     * @param message [java.lang.String]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:53
     */
    public static void sendUserInfo(String title, String message) {
        try {
            CommonUtils.log(message);
            CommonUtils.sleep(1000);//间歇发送息知，避免频繁发送提示导致消息漏发
            title = title + "，详细内容请点击查看";
            //请求息知
            sendUser.body("title=" + title + "&content=" + message + upInfo).execute();
        } catch (Exception e) {
            CommonUtils.error("息知发送失败(代号:1x003)，错误信息：" + e);
        }
    }

    /**
     * 发送登录请求
     *
     * @param qrCodeUrl [java.lang.String]
     * @return void
     * @author IDeLoveYou
     * @since 2023/8/13 1:39
     */
    public static void sendAdminLoginRequest(int qrCodeCount,String qrCodeUrl) {
        try {
            CommonUtils.log("请留意微信信息进行登录验证(第" + qrCodeCount + "次)，这可能需要1-2分钟");
            CommonUtils.sleep(1000);//间歇发送息知，避免频繁发送提示导致消息漏发
            //请求息知
            String content =
                    """
                    ### 登录流程
                    ```
                    1. 长按保存图片
                    2. 长按识别图中二维码打开企业微信
                    3. 使用企业微信扫描保存的二维码进行登录
                    ```
                    ||||
                    |-|-|-|
                    ||![登录二维码](""" + qrCodeUrl + """
                    )||
                    ||||
                    > 什么情况下我会收到这条信息？
                    - 当您第一次使用SGU-Water-and-Electricity-Bill-enquiry时会被要求该登录。
                    - 若学校机房发生断网、断电等不可控因素时您也会被要求重新登录。
                    -  请放心，我们不会收集任何有关您的个人隐私信息。
                    > 愿你在韶院发光发亮
                    > version:""" + MainClass.version;
            sendAdmin.body("title=请点击进行登录验证(第" + qrCodeCount + "次)&content=" + content).execute();
        } catch (Exception e) {
            CommonUtils.error("息知发送失败(代号:1x004)，错误信息：" + e);
        }
    }
}
