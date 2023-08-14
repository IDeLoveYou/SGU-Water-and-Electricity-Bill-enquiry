package cn.rabig.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CommonUtils {
    /**
     * 打印日志
     *
     * @param str [java.lang.String]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:52
     */
    public static void log(String str) {
        Console.log("[" + DateUtil.now() + "] [Info] " + str);
    }

    /**
     * 打印错误日志
     *
     * @param str [java.lang.String]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:52
     */
    public static void error(String str) {
        Console.error("[" + DateUtil.now() + "] [Error] " + str);
    }

    /**
     * 统一处理线程睡眠异常
     *
     * @param time [int]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:52
     */
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            SendMess.sendAdminError("脚本发生异常(代号:0x001)，脚本立即退出，错误信息：" + e + "\n");
            CommonUtils.error(e.toString());
            System.exit(1);//异常退出
        }
    }

    /**
     * 退出脚本
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:52
     */
    public static void exit() {
        CommonUtils.printProgressBar(10, "脚本10s后退出");
        System.exit(0);//正常退出
    }

    /**
     * 打印进度条
     *
     * @param time [int]
     * @param info [java.lang.String]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:52
     */
    public static void printProgressBar(int time, String info) {
        ProgressBarBuilder pbb = new ProgressBarBuilder()
                .setStyle(ProgressBarStyle.ASCII)
                .setTaskName("[Exit] " + info)
                .setUnit("s", 1);
        ProgressBar.wrap(IntStream.rangeClosed(1, time).boxed().collect(Collectors.toList()), pbb).forEach(i -> sleep(1000));
    }
}
