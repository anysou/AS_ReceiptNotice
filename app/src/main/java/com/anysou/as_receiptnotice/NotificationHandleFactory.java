package com.anysou.as_receiptnotice;


import android.app.Notification;
import android.os.Build;
import android.provider.Telephony;

import androidx.annotation.RequiresApi;

/**
 * 通知内容委托处理，分类处理工厂
 *
 * android 监听微信，QQ，微博，钉钉，淘宝，支付宝等消息  https://blog.csdn.net/wengsheng147/article/details/99435107
 * */

public  class NotificationHandleFactory{

    public static String NOTIFICATION_PACKAGE_EMAIL = "com.android.email";  //email
    public static String NOTIFICATION_PACKAGE_FACEBOOK = "com.facebook";    //facebook 脸书 美国的一个社交网络服务网站
    public static String NOTIFICATION_PACKAGE_INSTAGRAM = "com.instagram.android"; //Instagram（照片墙）是一款运行在移动端上的社交应用，以一种快速、美妙和有趣的方式将你随时抓拍下的图片彼此分享。
    public static String NOTIFICATION_PACKAGE_KAKAOTALK = "com.kakao.talk"; //「Kakao Talk」是一款来自韩国的由中国腾讯担任第二大股东的免费聊天软件,类似于QQ微信的聊天软件,
    public static String NOTIFICATION_PACKAGE_LINE = "jp.naver.line.android";  //LINE是韩国互联网集团NHN的日本子公司NHN Japan推出的一款即时通讯软件
    public static String NOTIFICATION_PACKAGE_MMS = "com.android.mms";  //短信

    public static String NOTIFICATION_PACKAGE_SKYPE = "com.skype";  //skype
    public static String NOTIFICATION_PACKAGE_SNAPCHAT = "com.snapchat.android"; //Snapchat(色拉布)是由斯坦福大学两位学生开发的一款“阅后即焚”照片分享应用
    public static String NOTIFICATION_PACKAGE_TWITTER = "com.twitter.android"; //推特
    public static String NOTIFICATION_PACKAGE_WHATSAPP = "com.whatsapp"; //WhatsApp（瓦次普）是一款非常受欢迎的跨平台应用程序，用于智能手机之间的通讯。

    public static String NOTIFICATION_PACKAGE_RIMET = "com.alibaba.android.rimet";//钉钉
    public static String NOTIFICATION_PACKAGE_WEIBO = "com.sina.weibo";//微博
    public static String NOTIFICATION_PACKAGE_QZONE = "com.qzone";//QQ空间
    public static String NOTIFICATION_PACKAGE_TAOBAO = " com.taobao.taobao";//淘宝
    public static String NOTIFICATION_PACKAGE_MOBILEQQ = "com.tencent.mobileqq"; //手机QQ

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public NotificationHandle getNotificationHandle(String pkg, Notification notification, IDoPost postpush){

        //支付宝
        if("com.eg.android.AlipayGphone".equals(pkg)){
            return new NotificationHandleAlipay("com.eg.android.AlipayGphone",notification,postpush);
        }
        //微信
        if("com.tencent.mm".equals(pkg)){
            return new NotificationHandleWechat("com.tencent.mm",notification,postpush);
        }
        //收钱吧
        if("com.wosai.cashbar".equals(pkg)){
            return new NotificationHandleCashbar("com.wosai.cashbar",notification,postpush);
        }
        //云闪付
        if("com.unionpay".equals(pkg)){
            return new NotificationHandleUnionpay("com.unionpay",notification,postpush);
        }
        //工银商户之家
        if("com.icbc.biz.elife".equals(pkg)){
            return new NotificationHandleIcbcelife("com.icbc.biz.elife",notification,postpush);
        }
        //接到短信
        if(getMessageAppPkg().equals(pkg)){
            return new NotificationHandleBanksProxy(getMessageAppPkg(),notification,postpush);
        }
        //应用管理GCM代收
        if("android".equals(pkg)){
            return new NotificationHandleXposedmodule("github.tornaco.xposedmoduletest",notification,postpush);
        }
        //小米的mipush
        if("com.xiaomi.xmsf".equals(pkg)){
            return  new NotificationHandleMipush("com.xiaomi.xmsf",notification,postpush);
        }


        return null;  //不是收款类APP消息
    }

    //获取短信
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //KitKat	4.4	2013年7月24日	API level 19
    private String getMessageAppPkg(){
        return Telephony.Sms.getDefaultSmsPackage(MainApplication.getAppContext());
    }

}


