package cn.rabig.check;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.rabig.main.MainClass;
import cn.rabig.utils.CommonUtils;
import cn.rabig.utils.QueryHttpRequest;
import cn.rabig.utils.SendMess;

import java.math.BigDecimal;

public class SettingCheck {
    /**
     * 测试sessionId参数
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:50
     */
    public static void checkSessionIdAndBuilding() {
        String responseBody = null;
        try {
            responseBody = HttpRequest
                    .post("http://210.38.192.117/wechat/basicQuery/queryElecBuilding.html")
                    .cookie("JSESSIONID=" + MainClass.sessionId)
                    .keepAlive(false)
                    .body("aid=0030000000002505&area={\"area\":\"1\",\"areaname\":\"韶关学院\"}")
                    .timeout(20000)
                    .execute().body();
        } catch (Exception e) {
            CommonUtils.error("请检查网络连接，企业微信水电费查询是否能够正常使用，或者学校可能已经更换查询接口（几率较低）");
            CommonUtils.exit();
        }
        if ("{\"errmsg\":\"会话已超时，请尝试重新访问业务应用。\",\"retcode\":\"91001\"}".equals(responseBody)) {
            CommonUtils.error("sessionId已过期，请检查sessionId");
            CommonUtils.exit();
        }
        JSONArray buildingList = JSONUtil.parseObj(responseBody).getJSONArray("buildingtab");
        boolean find = buildingList.stream().anyMatch(building -> {
            JSONObject buildingInFo = JSONUtil.parseObj(building);
            QueryHttpRequest.buildingId = (String) buildingInFo.get("buildingid");
            return MainClass.building.equals(buildingInFo.get("building"));
        });
        if (!find) {
            CommonUtils.error("请检查建筑名称是否正确，详细建筑名称格式在README.md");
            CommonUtils.exit();
        }
    }

    /**
     * 测试room参数
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:50
     */
    public static void checkRoom() {
        for (int i = 5; i <= 6; i++) {
            QueryHttpRequest.queryFeeRequest.body("aid=003000000000250" + i + "&" +
                    "area={\"area\":\"1\",\"areaname\":\"韶关学院\"}&" +
                    "building={\"building\":\"\",\"buildingid\":\"" + QueryHttpRequest.buildingId + "\"}&" +
                    "floor={\"floorid\":\"\",\"floor\":\"\"}&" +
                    "room={\"room\":\"\",\"roomid\":\"" + MainClass.room + "\"}");
            String responseBody = QueryHttpRequest.queryFeeRequest.execute().body();
            JSONObject responseJson = JSONUtil.parseObj(responseBody);
            if ("无法获取房间信息".equals(responseJson.get("errmsg"))) {
                CommonUtils.error("无法获取房间信息，请检查房间号是否正确");
                CommonUtils.exit();
            }
            switch (i) {
                case 5 -> {
                    CommonUtils.log(responseJson.get("errmsg") + "，请自行检查是否正确");
                    QueryHttpRequest.newElectricityFee = new BigDecimal(StrUtil.subBetween(responseJson.get("errmsg").toString(), ":", "元"));
                }
                case 6 -> {
                    CommonUtils.log(responseJson.get("errmsg") + "，请自行检查是否正确");
                    QueryHttpRequest.newWaterFee = new BigDecimal(StrUtil.subBetween(responseJson.get("errmsg").toString(), ":", "元"));
                }
            }
        }
    }

    /**
     * 测试room参数
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:50
     */
    private static void checkWeekDay() {
        if (MainClass.weekDay < 0 || MainClass.weekDay > 7) {
            CommonUtils.error("weekDay参数错误，请于1-7取值，星期日为1，星期一为2，星期二为3，以此类推");
            CommonUtils.exit();
        }
        switch (MainClass.weekDay) {
            case 1 -> MainClass.weekDayString = "MON";
            case 2 -> MainClass.weekDayString = "TUE";
            case 3 -> MainClass.weekDayString = "WED";
            case 4 -> MainClass.weekDayString = "THU";
            case 5 -> MainClass.weekDayString = "FRL";
            case 6 -> MainClass.weekDayString = "SAT";
            case 7 -> MainClass.weekDayString = "SUN";
        }
    }

    /**
     * 测试Url参数
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:50
     */
    private static void checkUrl() {
        if (MainClass.checkUrlEnable) {
            try {
                //请求息知
                SendMess.sendError.body("title=韶关学院水电费提醒脚本管理员通知(v" + MainClass.version + ")&" +
                        "content=" + SendMess.adminInfo).execute();
                SendMess.sendMessage.body("title=\n欢迎使用韶关学院水电费提醒脚本(v" + MainClass.version + ")&" +
                        "content=" + SendMess.upInfo).execute();
            } catch (Exception e) {
                CommonUtils.error("息知发送失败，请检查用户以及管理员的推送Url");
                CommonUtils.exit();
            }
            CommonUtils.log("已往息知发送测试信息，请自行查看是否收到");
        }
    }

    /**
     * 测试waitTime参数
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:51
     */
    private static void checkWaitTime() {
        if (MainClass.waitTime == null || MainClass.waitTime < 2) {
            CommonUtils.error("waitTime不宜低于两小时，由于充值费用后，学校数据库起码要3-4小时才会更新数据，因此waitTime时间太短的话会导致疯狂重复提示");
            CommonUtils.exit();
        }
    }

    /**
     * 测试lowMoney参数
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:51
     */
    private static void checkLowMoney() {
        if (MainClass.electricityLowMoney == null || MainClass.waterLowMoney == null) {
            CommonUtils.error("lowMoney不得为空");
            CommonUtils.exit();
        }
    }

    /**
     * 初始化和测试
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:51
     */
    public static void initAndCheck() {
        CommonUtils.log("参数测试程序启动");
        checkSessionIdAndBuilding();
        checkWeekDay();
        checkWaitTime();
        checkLowMoney();
        checkRoom();
        checkUrl();
        CommonUtils.log("所有参数检测完成，即将启动脚本\n");
        Console.log("""
                                 ,---.                      .=-.-.       _,---.  \s
                  .-.,.---.    .--.'  \\         _..---.    /==/_ /   _.='.'-,  \\ \s
                 /==/  `   \\   \\==\\-/\\ \\      .' .'.-. \\  |==|, |   /==.'-     / \s
                |==|-, .=., |  /==/-|_\\ |    /==/- '=' /  |==|  |  /==/ -   .-'  \s
                |==|   '='  /  \\==\\,   - \\   |==|-,   '   |==|- |  |==|_   /_,-. \s
                |==|- ,   .'   /==/ -   ,|   |==|  .=. \\  |==| ,|  |==|  , \\_.' )\s
                |==|_  . ,'.  /==/-  /\\ - \\  /==/- '=' ,| |==|- |  \\==\\-  ,    ( \s
                /==/  /\\ ,  ) \\==\\ _.\\=\\.-' |==|   -   /  /==/. /   /==/ _  ,  / \s
                `--`-`--`--'   `--`         `-._`.___,'   `--`-`    `--`------'  \s
                """);
    }
}
