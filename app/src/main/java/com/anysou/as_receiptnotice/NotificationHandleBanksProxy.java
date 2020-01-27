package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;

/**
 * 银行短信
 * */
@RequiresApi(api = Build.VERSION_CODES.KITKAT) //KitKat	4.4	2013年7月24日	API level 19
class NotificationHandleBanksProxy extends NotificationHandle {

    //定义银行信息识别器
    private BankDistinguisher onedistinguisher = new BankDistinguisher();

    public NotificationHandleBanksProxy(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    // 根据短信内容识别银行名称，内容里有： 信息内有“银行” 、“收入/存入/转入/入账/来帐”、银行名称
    private String getBankType(){
        return onedistinguisher.distinguishByMessageContent(content);
    }

    @Override
    public void handleNotification() {

        LogUtil.debugLog("可能是银行短信");

        String banktype = getBankType();
        if(banktype==null)  //没有银行名称，则返回
            return;

        String type = null;
        if(banktype=="")
            type="message-bank";
        else
            type="message-bank-"+banktype;

        LogUtil.debugLog("确定是银行短信，准备POST数据");
        Map<String,String> postmap=new HashMap<String,String>();
        postmap.put("type",type);
        postmap.put("time",onedistinguisher.extractTime(content,notitime));
        postmap.put("title","短信银行卡入账");
        postmap.put("phonenum",title);
        postmap.put("money",onedistinguisher.extractMoney(content));
        postmap.put("cardnum",onedistinguisher.extractCardNum(content));
        postmap.put("content",content);
        postpush.doPost(postmap);
        return ;
    }
}
