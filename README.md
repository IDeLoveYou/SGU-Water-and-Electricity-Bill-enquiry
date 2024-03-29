# 韶关学院水电费提醒脚本

## 目录

* [关于](#关于)

  * [运行环境](#运行环境)

  * [功能](#功能)

* [严肃警告](#严肃警告)

* [配置文件](#配置文件)

* [使用教程](#使用教程)

  * [jar](#jar)

  * [exe](#exe)

* [故障排除](#故障排除)

# 关于

## 运行环境

* jdk17

## 功能

1. 在水电费余额低于指定金额时，发送微信提醒给舍友（基础功能）

2. 周报功能，每周汇总水电费用量并通过微信发送提醒（可选开启）

3. 充值提醒功能，当用户充值完成后，脚本会通过微信发送消息给用户（可选开启）

4. 更换二维码扫码登录，不用麻烦的抓包

5. 脚本建议在服务器使用，若你的电脑从来不关机也可以在电脑挂着脚本

# 严肃警告

* 该脚本纯属个人学习交流使用，不允许任何人使用本脚本非法获取各宿舍水电费

# 配置文件

```ini
room = 119    例如: 119
building = 丁香C栋   例如: 丁香C栋
adminUrl = https://xizhi.qqoq.net/xxxxxx.send    管理员推送服务url，息知-单点推送
userUrl = https://xizhi.qqoq.net/xxxxxx.channel    用户推送服务url，息知-频道推送，将二维码发给舍友，让舍友也能收到通知
electricityLowMoney = 20    电费低于此额度提醒(元)，推荐值20
waterLowMoney = 10    水费低于此额度提醒(元)，推荐值10
waitTime = 6    欠费信息推送后与下次推送的间隔时间(小时)，推荐值6
checkUrlEnable = true    是否检测息知能否收到信息（填true或者false，第一次使用建议开启，后续建议不开启）
feeUpEnable = true    是否开启充值提醒功能(填true或者false)
weekEnable = true    是否开启周报功能(填true或者false)
weekDay = 7    周报功能推送日期（1表示周一，2表示周二）
verificationCount = 5    当账号掉线，脚本会重新向管理员发送登录二维码请求登录，单个二维码有效期5分钟，过期即重新发送新二维码，为防止骚扰，添加了发送次数限制
```

* adminUrl 以及userUrl 参数请使用息知的提醒，官网链接：[https://xz.qqoq.net/](https://xz.qqoq.net/ "https://xz.qqoq.net/")

* room参数以你在企业微信查询水电费时填写的房间号为准

* building请使用以下字段

```
海棠B栋 |  碧桃24栋 |  红棉西栋 |  秋枫A栋 |  紫荆B栋 |  丁香A栋 |  秋枫D栋 |  碧桃28栋 |  碧桂A栋 |  丹竹A栋 |  丹枫A栋 |  丹桂22栋 |  芙蓉D栋 |  黄田坝9栋 |  丹枫B栋 |  芙蓉B栋 |  红棉东栋 |  黄田坝6栋 |  碧桂C栋 |  蔷薇C栋 |  秋枫C栋 |  黄田坝10栋 |  丹桂26栋 |  碧桃21栋 |  秋枫B栋 |  碧桂B栋 |  蔷薇B栋 |  紫薇A栋 |  芙蓉A栋 |  丹桂23栋 |  海棠A栋 |  紫藤1栋 |  银杏A栋 |  丁香D栋 |  紫薇C栋 |  碧桃27栋 |  碧桃29栋 |  丁香F栋 |  丁香G栋 |  红枫B栋 |  紫薇B栋 |  紫荆C栋 |  银杏B栋 |  丹竹B栋 |  蔷薇A栋 |  紫荆A栋 |  梧桐苑栋 |  海棠C栋 |  紫竹A栋 |  樱花苑栋 |  碧桃20栋 |  丁香B栋 |  丁香E栋 |  芙蓉C栋 |  丁香C栋 |  红枫A栋 |  紫竹B栋 |  丹竹C栋 |  黄田坝12栋 |  碧桃25栋
```

# 使用教程

> 跳转至下载地址：[SGU-Water-and-Electricity-Bill-enquiry](../../releases)

## jar

* 拥有`jre`环境

* 修改`setting.ini`配置文件

* 使用`java -jar xxxxx.jar`命令来启动脚本，注意：`setting.ini`配置文件必须与jar包在同一文件夹

* 根据提示指引完成登录操作

## exe

* 若您有`jre/jdk`，可以选择删除压缩包中的`jre`，程序会自动搜索系统里的`jre`来运行脚本，若无的话，程序会使用压缩包自带的`jre`，请勿删除该文件夹，并将该文件夹与`SGU-Water-and-Electricity-Bill-enquiry-xxx.exe`置于同一文件夹

* 修改`setting.ini`配置文件

* 双击`SGU-Water-and-Electricity-Bill-enquiry-xxx.exe`文件运行

* 根据提示指引完成登录操作

# 故障排除

欢迎提pr，欢迎fork

有任何问题欢迎在Issues中提出，或者通过邮箱[rabig@foxmail.com](mailto:rabig@foxmail.com "rabig@foxmail.com")联系我

