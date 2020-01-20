package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信收款
 * */

class NotificationHandleWechat extends NotificationHandle {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public NotificationHandleWechat(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    @Override
    public void handleNotification() {
        if((title.contains("微信支付")|title.contains("微信收款"))&&content.contains("收款")){
            Map<String,String> postmap=new HashMap<String,String>();
            postmap.put("type","wechat");
            postmap.put("time",notitime);
            postmap.put("title",title);
            postmap.put("money",extractMoney(content));
            postmap.put("content",content);
            postpush.doPost(postmap);
            return ;
        }
    }
}
