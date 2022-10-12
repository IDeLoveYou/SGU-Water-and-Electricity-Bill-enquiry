package cn.rabig.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import cn.rabig.main.MainClass;

import java.math.BigDecimal;

public class QueryHttpRequest {
    public static String buildingId;
    public static BigDecimal oldElectricityFee;
    public static BigDecimal oldWaterFee;
    public static BigDecimal newElectricityFee;
    public static BigDecimal newWaterFee;
    public static volatile boolean waitFlag = true;//用于等待低额提醒后等待是否结束
    public static BigDecimal weekElectricityFee = new BigDecimal("0");
    public static BigDecimal weekWaterFee = new BigDecimal("0");
    public static HttpRequest queryFeeRequest;

    //初始化请求头
    static {
        queryFeeRequest = HttpRequest
                .post("http://210.38.192.117/wechat/basicQuery/queryElecRoomInfo.html")
                .cookie("JSESSIONID=" + MainClass.sessionId)
                .keepAlive(true)
                .timeout(20000);//超时，毫秒
    }

    /**
     * 获取水电费
     *
     * @return void
     * @author MoNo
     * @since 2022/10/12 22:53
     */
    public static void queryFees() {
        try {
            for (int i = 5; i <= 6; i++) {
                queryFeeRequest.body("aid=003000000000250" + i + "&" +
                        "area={\"area\":\"1\",\"areaname\":\"韶关学院\"}&" +
                        "building={\"building\":\"\",\"buildingid\":\"" + buildingId + "\"}&" +
                        "floor={\"floorid\":\"\",\"floor\":\"\"}&" +
                        "room={\"room\":\"\",\"roomid\":\"" + MainClass.room + "\"}");
                String errmsg = JSONUtil.parseObj(queryFeeRequest.execute().body()).get("errmsg").toString();
                if ("会话已超时，请尝试重新访问业务应用。".equals(errmsg)) {
                    SendMess.sendError("sessionId已过期，脚本即将退出");
                    CommonUtils.exit();
                } else if (!errmsg.contains("元")) {
                    throw new Exception();//获取到的数据不符合预期
                }
                switch (i) {
                    case 5 -> {
                        oldElectricityFee = newElectricityFee;
                        newElectricityFee = new BigDecimal(StrUtil.subBetween(errmsg, ":", "元"));
                    }
                    case 6 -> {
                        oldWaterFee = newWaterFee;
                        newWaterFee = new BigDecimal(StrUtil.subBetween(errmsg, ":", "元"));
                    }
                }
            }
        } catch (Exception e) {
            CommonUtils.error("获取水电费信息失败，错误信息：" + e + "\n脚本尝试重连");//出现函数处理错误
            CommonUtils.sleep(10000);
            queryFees();
        }
    }
}
