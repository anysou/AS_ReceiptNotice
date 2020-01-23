package com.anysou.as_receiptnotice;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 获取 存储的设置配置
 * */

public class PreferenceUtil {

    SharedPreferences sharedPref = null;
    Context context = null;

    public PreferenceUtil(Context context) {
        this.context = context;
        init();
    }

    //getDefaultSharedPreferences： 1）获取到全局作用域的preference (包名一 样的可以取到) 2）将使用默认名称，如“包名_Preferences”
    public void init() { sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context); }



    // 设置标识设备ID
    public String getDeviceid() {
        return this.sharedPref.getString("deviceid", "");
    }
    // 在设置的标识设备ID后面，再加上唯一的UUID
    public boolean isAppendDeviceiduuid() { return this.sharedPref.getBoolean("isappenddeviceiduuid", false);  }
    // 是否跳过设备ID加密
    public boolean isSkipEncryptDeviceid() { return this.sharedPref.getBoolean("isskipencryptdeviceid", false); }



    // 是否要加密
    public boolean isEncrypt() {
        return this.sharedPref.getBoolean("isencrypt", false);
    }
    // 加密方法（des,md5）
    public String getEncryptMethod() {
        return this.sharedPref.getString("encryptmethod", null);
    }
    // 密钥
    public String getPasswd() {
        return this.sharedPref.getString("passwd", null);
    }



    // 是否设置CPU不休眠状态
    public boolean isWakelock() {
        return this.sharedPref.getBoolean("iswakelock", false);
    }

    // 要推送的自定义选项（用冒号区分键和值，多个使用分号）
    public String getCustomOption() {
        return this.sharedPref.getString("custom_option", "");
    }



    // 是否要 ECHO 实时通信服务
    public boolean isEcho() {
        return this.sharedPref.getBoolean("isecho", false);
    }
    // ECHO 服务器
    public String getEchoServer() {
        return this.sharedPref.getString("echoserver", null);
    }
    // ECHO 间隔时间 频率
    public String getEchoInterval() {
        return this.sharedPref.getString("echointerval", "");
    }


    // 是否推送后移除通知
    public boolean isRemoveNotification() { return this.sharedPref.getBoolean("isremovenotification", false); }
    // 是否推送失败后重复推送
    public boolean isPostRepeat() {
        return this.sharedPref.getBoolean("ispostrepeat", false);
    }
    // 重复推送次数
    public String getPostRepeatNum() {
        return this.sharedPref.getString("postrepeatnum", "3");
    }

    //是否在推送时信任所有证书
    public boolean isTrustAllCertificates() { return this.sharedPref.getBoolean("istrustallcertificates", false); }
}
