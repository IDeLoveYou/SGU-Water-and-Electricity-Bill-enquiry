package cn.rabig.check;

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.rabig.main.MainClass;
import cn.rabig.utils.CommonUtils;
import cn.rabig.utils.LoginUtils;
import cn.rabig.utils.QueryHttpRequest;
import cn.rabig.utils.SendMess;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;
import java.util.Objects;

public class SettingCheck {
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
                SendMess.sendAdmin.body("title=韶关学院水电费提醒脚本管理员通知(v" + MainClass.version + ")，息知收发检查&" +
                        "content=" + SendMess.adminInfo).execute();
                SendMess.sendUser.body("title=\n韶关学院水电费提醒脚本用户通知(v" + MainClass.version + ")，息知收发检查&" +
                        "content=" + SendMess.upInfo).execute();
            } catch (Exception e) {
                CommonUtils.error("息知发送失败，请检查用户以及管理员的推送Url");
                CommonUtils.exit();
            }
            CommonUtils.log("已往息知发送测试信息，请自行查看是否收到");
        }
    }

    /**
     * 测试building参数
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
                    .cookie(LoginUtils.JSESSIONID)
                    .keepAlive(false)
                    .body("aid=0030000000002505&area={\"area\":\"1\",\"areaname\":\"韶关学院\"}")
                    .timeout(20000)
                    .execute().body();
        } catch (Exception e) {
            CommonUtils.error("请检查网络连接，企业微信水电费查询是否能够正常使用，或者学校可能已经更换查询接口（几率较低）");
            CommonUtils.exit();
        }
        JSONArray buildingList = Objects.requireNonNull(JSONObject.parseObject(responseBody)).getJSONArray("buildingtab");
        boolean find = buildingList.stream().anyMatch(building -> {
            JSONObject buildingInfo = JSONObject.parseObject(building.toString());
            QueryHttpRequest.buildingId = buildingInfo.getString("buildingid");
            return MainClass.building.equals(buildingInfo.getString("building"));
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
            JSONObject responseJson = JSONObject.parseObject(responseBody);
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
     * 测试WeekDay参数
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
        //首先检查息知是否能正常接收信息，确保进行登录验证
        checkUrl();
        //开始登录
        LoginUtils.qrLogin();
        //登录成功检查参数
        checkSessionIdAndBuilding();
        checkWeekDay();
        checkWaitTime();
        checkLowMoney();
        checkRoom();
        CommonUtils.log("所有参数检测完成，即将启动脚本\n");
        Console.log("""
                 $$$$$$\\ $$$$$$$\\            $$\\                                 $$\\     $$\\                  \s
                 \\_$$  _|$$  __$$\\           $$ |                                \\$$\\   $$  |                 \s
                   $$ |  $$ |  $$ | $$$$$$\\  $$ |      $$$$$$\\ $$\\    $$\\  $$$$$$\\\\$$\\ $$  /$$$$$$\\  $$\\   $$\\\s
                   $$ |  $$ |  $$ |$$  __$$\\ $$ |     $$  __$$\\\\$$\\  $$  |$$  __$$\\\\$$$$  /$$  __$$\\ $$ |  $$ |
                   $$ |  $$ |  $$ |$$$$$$$$ |$$ |     $$ /  $$ |\\$$\\$$  / $$$$$$$$ |\\$$  / $$ /  $$ |$$ |  $$ |
                   $$ |  $$ |  $$ |$$   ____|$$ |     $$ |  $$ | \\$$$  /  $$   ____| $$ |  $$ |  $$ |$$ |  $$ |
                 $$$$$$\\ $$$$$$$  |\\$$$$$$$\\ $$$$$$$$\\\\$$$$$$  |  \\$  /   \\$$$$$$$\\  $$ |  \\$$$$$$  |\\$$$$$$  |
                 \\______|\\_______/  \\_______|\\________|\\______/    \\_/     \\_______| \\__|   \\______/  \\______/\s
                                                                                                              \s
                                                                                                              \s
                                                                                                              \s
                """);
    }
}
