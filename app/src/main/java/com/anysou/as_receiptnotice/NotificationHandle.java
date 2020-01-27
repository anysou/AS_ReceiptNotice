package com.anysou.as_receiptnotice;


import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知句柄  抽象类
 * */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public abstract class NotificationHandle{

    protected Notification notification;    // 通知
    protected String pkgtype;               // 通知的包名
    protected Bundle extras;                // 通知捆绑的
    protected String title;                 // 通知的标题
    protected String content;               // 通知的内容
    protected String notitime;              // 通知的时间
    protected IDoPost postpush;             // 处理通知得到的POST数据（接口）
    protected ActionStatusBarNotification actionstatusbar;  // Action组件用来移除通知用（接口）
    public StatusBarNotification sbn;       // 通知状态

    //                               包名             通知                 要Post的数据
    public NotificationHandle(String pkgtype, Notification notification, IDoPost postpush){
        this.pkgtype=pkgtype;
        this.notification=notification;
        this.postpush=postpush;
        this.extras=notification.extras;
        title = extras.getString(Notification.EXTRA_TITLE, "");  // 获取通知标题
        content = extras.getString(Notification.EXTRA_TEXT, ""); // 获取通知内容
        long when = notification.when;  // 通知的时间
        Date date = new Date(when);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        notitime = format.format(date); // 通知的时间已格式化
    }

    // 设置 sbn
    public void setStatusBarNotification(StatusBarNotification sbn){
        this.sbn=sbn;
    }

    // 设置 Action 组件
    public void setActionStatusbar(ActionStatusBarNotification actionstatusbar){
        this.actionstatusbar = actionstatusbar;
    }

    // 处理通知，抽象函数 （具体实现在各个收款子类中）
    public  abstract void handleNotification();

    // 获取收款额
    protected  String extractMoney(String content){
        Pattern pattern = Pattern.compile("(到账|收款|收款￥|向你付款|向您付款|入账|来帐)(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?元");
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            String tmp=matcher.group();
            Pattern patternnum = Pattern.compile("(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?");
            Matcher matchernum = patternnum.matcher(tmp);
            if(matchernum.find())
                return matchernum.group();
            return null;
        }else
            return null;
    }

    // 是否为收款信息
    protected boolean predictIsPost(String content){
        Pattern pattern = Pattern.compile("(到账|收到|收款|向你付款|向您付款|入账|来帐)(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?元");
        Matcher matcher = pattern.matcher(content);
        if(matcher.find())
            return true;
        else
            return false;
    }

    // 移除通知
    protected void removeNotification(){
        if(actionstatusbar==null|sbn==null)
            return ;
        if(predictIsPost(content))  //是收款类通知
            actionstatusbar.removeNotification(sbn);
    }

    // 打印收款类通知的信息
    @RequiresApi(api = Build.VERSION_CODES.KITKAT) //KitKat	4.4	2013年7月24日	API level 19
    protected void printNotify(){
        LogUtil.debugLog("-----------------");
        LogUtil.debugLog("接受到支付类app消息");
        LogUtil.debugLog("包名是"+this.pkgtype);
        NotificationUtil.printNotify(notification);
        LogUtil.debugLog("**********************");
    }

}


