package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;


/**
 * 云闪付收款
 * */


class NotificationHandleUnionpay extends NotificationHandle {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public NotificationHandleUnionpay(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    @Override
    public void handleNotification() {
        if(title.contains("消息推送")&&content.contains("云闪付收款")){
            Map<String,String> postmap=new HashMap<String,String>();
            postmap.put("type","unionpay");
            postmap.put("time",notitime);
            postmap.put("title",title);
            postmap.put("money",extractMoney(content));
            postmap.put("content",content);
            postpush.doPost(postmap);
            return ;
        }
    }
}
