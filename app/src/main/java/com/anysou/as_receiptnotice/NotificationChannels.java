package com.anysou.as_receiptnotice;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;

/***
 * 建立 本APP 所用到的 通知渠道
 *
 * 在Android O版本中，发送通知的时候必须要为通知设置通知渠道，否则通知不会被发送。
 * 几步搞定Service不被杀死  https://blog.csdn.net/cxq234843654/article/details/43058333
 */


public class NotificationChannels {

    public final static String CRITICAL_ID = "critical_id";  //定义重要通知渠道ID名
    public final static String DEFAULT_ID = "default_id";    //定义默认渠道ID名

    // 建立通知渠道
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createAllNotificationChannels(Context context) {
        // 获取通知管理器
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm == null) {
            return;
        }

        // 创建一个通知渠道至少需要渠道ID、渠道名称以及重要等级这三个参数，
        // 其中渠道ID可以随便定义，只要保证全局唯一性就可以。
        // 渠道名称是给用户看的，需要能够表达清楚这个渠道的用途。
        // 重要等级的不同则会决定通知的不同行为，当然这里只是初始状态下的重要等级，用户可以随时手动更改某个渠道的重要等级，App是无法干预的

        NotificationChannel criticalChannel = new NotificationChannel(
                CRITICAL_ID,
                context.getString(R.string.channel_critical),
                NotificationManager.IMPORTANCE_HIGH);
        // 配置通知渠道的属性
        criticalChannel.setDescription("这是发送重要通知信息的通道");
        // 设置通知出现时声音，默认通知是有声音的
        criticalChannel.setSound(null,null);
        // 设置通知出现时的闪灯（如果 android 设备支持的话）
        criticalChannel.enableLights(true);
        criticalChannel.setLightColor(Color.RED);
        // 设置通知出现时的震动（如果 android 设备支持的话）
        criticalChannel.enableVibration(true);
        criticalChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

        // 建立所有通道
        nm.createNotificationChannels(Arrays.asList(
                criticalChannel,  //通道1
                new NotificationChannel(  //通道2
                        DEFAULT_ID,
                        context.getString(R.string.channel_default),
                        NotificationManager.IMPORTANCE_DEFAULT)
        ));

    }

}
