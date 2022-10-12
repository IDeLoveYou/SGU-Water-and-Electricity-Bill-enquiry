package cn.rabig.utils;

import cn.hutool.http.HttpRequest;
import cn.rabig.main.MainClass;

public class SendMess {
    public static final String upInfo = "||更新内容(v" + MainClass.version + ")|\n" +
            """
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
                    > 愿你在韶院发光发亮
                    """;

    public static final String adminInfo =
            """
                    |代号|错误提示|
                    |-|-|
                    |0x001|CommonUtils.exit()函数出现错误|
                    |1x001|SendMess.sendError()息知管理员信息发送失败|
                    |1x002|SendMess.sendMessage()息知用户信息发送失败|
                    > 管理员提示
                    """;

    public static HttpRequest sendError;
    public static HttpRequest sendMessage;

    static {
        sendError = HttpRequest.post(MainClass.adminUrl).keepAlive(true).timeout(20000);
        sendMessage = HttpRequest.post(MainClass.userUrl).keepAlive(true).timeout(20000);
    }

    /**
     * 发送管理员信息
     *
     * @param error [java.lang.String]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:53
     */
    public static void sendError(String error) {
        try {
            CommonUtils.error(error);
            CommonUtils.sleep(1000);//间歇发送息知，避免频繁发送提示导致消息漏发
            String title = error + "\n\n详细错误提示请点击查看";
            //请求息知
            sendError.body("title=" + title + "&" +
                    "content=" + adminInfo).execute();
        } catch (Exception e) {
            CommonUtils.error("息知发送失败(代号:1x001)，错误信息：" + e);
        }

    }

    /**
     * 发送用户信息
     *
     * @param message [java.lang.String]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:53
     */
    public static void sendMessage(String message) {
        try {
            CommonUtils.log(message);
            CommonUtils.sleep(1000);//间歇发送息知，避免频繁发送提示导致消息漏发
            String title = "\n" + message + "\n\nversion: " + MainClass.version + "，详细更新内容请点击查看";
            //请求息知
            sendMessage.body("title=" + title + "&" +
                    "content=" + upInfo).execute();
        } catch (Exception e) {
            CommonUtils.error("息知发送失败(代号:1x002)，错误信息：" + e);
        }
    }
}
