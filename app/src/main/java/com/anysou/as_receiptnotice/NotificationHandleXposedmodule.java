package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用管理GCM代收
 * */
@RequiresApi(api = Build.VERSION_CODES.KITKAT) //KitKat	4.4	2013年7月24日	API level 19
class NotificationHandleXposedmodule extends NotificationHandle {

    public NotificationHandleXposedmodule(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    @Override
    public void handleNotification() {
        if(content.contains("微信支付") && content.contains("收款")){
            Map<String,String> postmap=new HashMap<String,String>();
            postmap.put("type","wechat");
            postmap.put("time",notitime);
            postmap.put("title","微信支付");
            postmap.put("money",extractMoney(content));
            postmap.put("content",content);
            postpush.doPost(postmap);
            return ;
        }
    }
}
