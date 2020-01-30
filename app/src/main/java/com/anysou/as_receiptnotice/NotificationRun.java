package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * 创建一个发送通知、清除通知的类
 * 全面解析Notification  https://www.jianshu.com/p/c4dbf129115a
 * **/
public class NotificationRun {

    // 发送通知                    上下文      Android 8.0的渠道ID      小图标      标题         内容       是否点击自动删除      点击的动作    通知ID  是否设定不可删除标签
    public static Notification getNC(Context context,String channel_id,int smallIcon,String title,String text,Boolean autoCancel,PendingIntent pi,int id,boolean flags){

        if(smallIcon==0){
            smallIcon = R.drawable.log_icon;
        }
        if(title==null || title==""){
            title = "收款通知";
        }
        if(text==null || text==""){
            text = "您有一个收款通知";
        }
        if(autoCancel==null){
            autoCancel =  true;
        }
        if(pi==null){
            Intent ClickIntent = new Intent(context,MainActivity.class);
            pi = PendingIntent.getActivity(context, 1, ClickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        // 构建 Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(smallIcon)   //设置通知左侧的小图标
                .setContentTitle(title)   //设置通知标题
                .setContentText(text)     //设置通知内容
                .setAutoCancel(autoCancel)   //设置点击通知后自动删除通知
                .setContentIntent(pi)     //设置点击通知时的响应事件
                .setPriority(Notification.PRIORITY_HIGH)  //优先级
                .setWhen(System.currentTimeMillis())      //设置时间,long类型自动转换
                .setDefaults(Notification.DEFAULT_ALL);
        //兼容  API 16    android 4.1 Jelly Bean    果冻豆
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            builder.setShowWhen(true);      //设置显示通知时间
        }
        // 兼容  API 26，Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(!channel_id.equals(NotificationChannels.CRITICAL_ID))
                channel_id = NotificationChannels.DEFAULT_ID;
            builder.setChannelId(channel_id);
        }

        Notification notification = builder.build();

        if(flags) {  //标记不可删除通知
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            notification.flags |= Notification.FLAG_NO_CLEAR;
        }

        return notification;  // 通知
    }

    // 发送通知                    上下文      Android 8.0的渠道ID      小图标      标题         内容       是否点击自动删除      点击的动作    通知ID  是否设定不可删除标签
    public static void NCSend(Context context,String channel_id,int smallIcon,String title,String text,Boolean autoCancel,PendingIntent pi,int id,boolean flags){

        // 获取系统 通知管理 服务
        NotificationManager mNotifyMgr =  (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        // 获得通知构建
        Notification notification = getNC(context,channel_id,smallIcon,title,text,autoCancel,pi,id,flags);
        // 发送通知
        mNotifyMgr.notify(id, notification);
    }


    //清除指定ID通知
    public static void NCClearId(Context context,int id) {
        NotificationManager mNotifyMgr =  (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
    }

    //清除所有通知
    public static void NCClearAll(Context context) {
        NotificationManager mNotifyMgr =  (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancelAll();
    }
}
