package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 收钱吧
 * */
@RequiresApi(api = Build.VERSION_CODES.KITKAT) //KitKat	4.4	2013年7月24日	API level 19
class NotificationHandleCashbar extends NotificationHandle {

    public NotificationHandleCashbar(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    @Override
    public void handleNotification() {
        if(title.contains("收钱吧")){
            if(content.contains("成功收款") | content.contains("向你付款")){
                Map<String,String> postmap=new HashMap<String,String>();
                postmap.put("type",getCashbarType(content));
                postmap.put("time",notitime);
                postmap.put("title","收钱吧");
                postmap.put("money",extractMoney(content));
                postmap.put("content",content);
                postpush.doPost(postmap);
                return ;
            }
        }
    }

    // 来自哪里的收钱
    private String getCashbarType(String content){
        Pattern pattern = Pattern.compile("(来自)(微信|支付宝|.*)");
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            String tmp=matcher.group(2);
            return "cashbar-"+transType(tmp);
        }else
            return "";

    }
    // 收款类型
    private String transType(String chinesetype){
        if(chinesetype.equals("微信"))
            return "wechat";
        if(chinesetype.equals("支付宝"))
            return "alipay";
        else return chinesetype;
    }
}
