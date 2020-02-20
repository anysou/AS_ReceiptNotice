# AnyPay

AP收款古精灵：一篮子个人收款管理自动服务系统。

## 功能：
一、微信、支付宝、云闪付、QQ、收钱吧、工银商户之家、银行短信、等一系列个人收款的日志记录、回调通知、等管理服务。
二、微信、QQ、支付宝 抢红包。

## APP
**开发环境**
1、Android Studio 4.0

**运行环境**
1、Android 安卓系统
2、版本：JELLY_BEAN_MR2   API 17

**技术要点**

1、TLog 日志工具 记录 收款信息。
2、LiveEventBus 消息总线框架。
3、

**流程**

1、AndroidManifest.xml -> application -> android:name=".MainApplication"

  设置全局变量、全局方法、启动监测服务、初始化TLog(日志工具)、设置LiveEventBus(消息事件总线框架)

2、Android 9.0 http 网络请求的问题解决： AndroidManifest.xml -> application -> android:networkSecurityConfig="@xml/network_security_config"

3、.....