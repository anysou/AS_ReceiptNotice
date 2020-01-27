package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

/**
 * 工银商户
 * */

@RequiresApi(api = Build.VERSION_CODES.KITKAT) //KitKat	4.4	2013年7月24日	API level 19
class NotificationHandleIcbcelife extends NotificationHandle {

    public NotificationHandleIcbcelife(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    @Override
    public void handleNotification() {
        if(title.contains("工银商户")){
            if(content.contains("已收到")&&content.contains("元")){
                Map<String,String> postmap=new HashMap<String,String>();
                postmap.put("type","icbcelife");
                postmap.put("time",notitime);
                postmap.put("title","工银商户之家");
                postmap.put("money",extractMoney(content));
                postmap.put("content",content);

                postpush.doPost(postmap);
                return ;
            }
        }
    }
}
