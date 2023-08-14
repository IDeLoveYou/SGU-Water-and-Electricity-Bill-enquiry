package cn.rabig.main;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.setting.dialect.Props;
import cn.rabig.check.SettingCheck;
import cn.rabig.utils.CommonUtils;
import cn.rabig.utils.QueryHttpRequest;
import cn.rabig.utils.SendMess;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author: MoNo
 * @Date: 2022/8/2 12:15
 */
public class MainClass {
    public static String room;
    public static String building;
    public static String adminUrl;
    public static String userUrl;
    public static BigDecimal electricityLowMoney;
    public static BigDecimal waterLowMoney;
    public static Float waitTime;
    static boolean feeUpEnable;
    public static boolean checkUrlEnable;
    static boolean weekEnable;
    public static int weekDay;
    public static int verificationCount;
    public static String weekDayString;
    public static String version = "4.5.2";

    static {
        //初始化配置文件
        try {
            @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")//忽略集合props未更新警告
            Props props = new Props(System.getProperty("user.dir") + System.getProperty("file.separator") + "setting.ini", CharsetUtil.UTF_8);
            room = props.getProperty("room");
            building = props.getProperty("building");
            adminUrl = props.getProperty("adminUrl");
            userUrl = props.getProperty("userUrl");
            electricityLowMoney = props.getBigDecimal("electricityLowMoney");
            waterLowMoney = props.getBigDecimal("waterLowMoney");
            waitTime = props.getFloat("waitTime");
            feeUpEnable = props.getBool("feeUpEnable");
            checkUrlEnable = props.getBool("checkUrlEnable");
            weekEnable = props.getBool("weekEnable");
            weekDay = props.getInt("weekDay");
            verificationCount = props.getInt("verificationCount");
            if (StrUtil.hasBlank(room, building, adminUrl, userUrl)) {
                CommonUtils.error("请按照规定完善配置文件！");
                CommonUtils.exit();
            }
        } catch (Exception e) {
            CommonUtils.error("请检查配置文件是否存在并按照规定要求填写！");
            CommonUtils.exit();
        }
        //启动参数测试脚本
        SettingCheck.initAndCheck();
    }

    /**
     * 主函数
     *
     * @param args [java.lang.String]
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:51
     */
    public static void main(String[] args) {
        //初始化周报部分的两个定时任务
        if (weekEnable) {
            //每星期一清除一次周费
            CronUtil.schedule("0 0 0 ? * MON", (Task) () -> {
                QueryHttpRequest.weekElectricityFee = BigDecimal.valueOf(0);
                QueryHttpRequest.weekWaterFee = BigDecimal.valueOf(0);
            });
            //周费提醒
            CronUtil.schedule("0 0 8 ? * " + weekDayString, (Task) () -> SendMess.sendUserInfo("水电费周报", "本周电费花费：" + QueryHttpRequest.weekElectricityFee + "元\n水费花费：" + QueryHttpRequest.weekWaterFee + "元"));
            CronUtil.start();
        }
        //创建定时器循环主函数部分
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //查询水电费信息
                QueryHttpRequest.queryFees();

                //低额提醒部分
                if (QueryHttpRequest.waitFlag) {
                    String title = "";
                    String message = "";
                    if (QueryHttpRequest.newElectricityFee.compareTo(electricityLowMoney) < 0) {
                        title = "电费不足";
                        message = "- 电费剩余：" + QueryHttpRequest.newElectricityFee + "元，电费小于" + electricityLowMoney + "元\n";
                    }
                    if (QueryHttpRequest.newWaterFee.compareTo(waterLowMoney) < 0) {
                        title = title + "，水费不足";
                        message = message + "- 水费剩余：" + QueryHttpRequest.newWaterFee + "元，水费小于" + waterLowMoney + "元\n";
                    }
                    if (!message.isEmpty()) {
                        message = message + "- 脚本将在" + new DecimalFormat("###.##").format(waitTime) + "小时后再次提醒您，请及时充值\n- 若您已充值，请忽略此提醒\n- 企业微信更新余额存在延迟";
                        SendMess.sendUserInfo(title, message);
                        QueryHttpRequest.waitFlag = false;
                        new Thread(() -> {
                            CommonUtils.sleep((int) (waitTime * 3600 * 1000));
                            QueryHttpRequest.waitFlag = true;
                        }).start();
                    }
                }

                //充值提醒部分
                if (feeUpEnable) {
                    if (QueryHttpRequest.oldElectricityFee.compareTo(QueryHttpRequest.newElectricityFee) < 0) {
                        SendMess.sendUserInfo("电费充值成功", "电费充值成功，充值金额" + QueryHttpRequest.newElectricityFee.subtract(QueryHttpRequest.oldElectricityFee) + "元，信息可能有延迟");
                    }
                    if (QueryHttpRequest.oldWaterFee.compareTo(QueryHttpRequest.newWaterFee) < 0) {
                        SendMess.sendUserInfo("水费充值成功", "水费充值成功，充值金额" + QueryHttpRequest.newWaterFee.subtract(QueryHttpRequest.oldWaterFee) + "元，信息可能有延迟");
                    }
                }

                //周报提醒部分
                if (weekEnable) {
                    //周费计算部分
                    if (QueryHttpRequest.oldElectricityFee.compareTo(QueryHttpRequest.newElectricityFee) > 0) {
                        QueryHttpRequest.weekElectricityFee = QueryHttpRequest.oldElectricityFee.subtract(QueryHttpRequest.newElectricityFee).add(QueryHttpRequest.weekElectricityFee);
                    }
                    if (QueryHttpRequest.oldWaterFee.compareTo(QueryHttpRequest.newWaterFee) > 0) {
                        QueryHttpRequest.weekWaterFee = QueryHttpRequest.oldWaterFee.subtract(QueryHttpRequest.newWaterFee).add(QueryHttpRequest.weekWaterFee);
                    }
                }

                //脚本延迟
                CommonUtils.log("脚本运行中，请置于后台防止被杀进程");
            }
        }, 0, 600000);
    }
}
