package com.anysou.as_receiptnotice;



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

/***
 * 本地 广播接收器 类 继承   BroadcastReceiver
 * 但本地广播是采用动态注册，不需要再 AndroidManifest.xml 进行静态注册。
 */

class LocalBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //逻辑代码
        String text = intent.getStringExtra("text");  //获取本地广播 的 text 的值
        Log.i("test", text);
        Toast.makeText(context,text,Toast.LENGTH_LONG).show();

        if(text.contains("后台运行")){
            //NotificationRun.NCSend(context,NotificationChannels.DEFAULT_ID,0,"我在后台","点击切换到前台！",true,null,1);

            NotificationRun.NCSend(context,NotificationChannels.CRITICAL_ID,0,"我在后台","点击切换到前台！",false,null,1,true);
        }

        if(text.contains("aaaaaaaaaa")){
            // 获取系统 通知管理 服务
            NotificationManager mNotifyMgr =  (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            Intent ClickIntent = new Intent(context,MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 1, ClickIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            // 构建 Notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setSmallIcon(R.drawable.log_icon)  //设置通知左侧的小图标
                    .setContentTitle("我在后台")        //设置通知标题
                    .setContentText("点击切换到前台!")     //设置通知内容
                    .setAutoCancel(true)     //设置点击通知后自动删除通知
                    .setContentIntent(pi)    //设置点击通知时的响应事件
                    .setPriority(Notification.PRIORITY_HIGH)  //优先级
                    .setWhen(System.currentTimeMillis())     //设置时间,long类型自动转换
                    .setDefaults(Notification.DEFAULT_ALL);
            //兼容  API 16    android 4.1 Jelly Bean    果冻豆
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                builder.setShowWhen(true);      //设置显示通知时间
            }
            // 兼容  API 26，Android 8.0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                // 创建一个通知渠道至少需要渠道ID、渠道名称以及重要等级这三个参数，
                NotificationChannel notificationChannel = new NotificationChannel("AppNotificationId", "NotificationName", NotificationManager.IMPORTANCE_DEFAULT);
                mNotifyMgr.createNotificationChannel(notificationChannel); // 注册通道，注册后除非卸载再安装否则不改变
                builder.setChannelId("AppNotificationId");
            }
            mNotifyMgr.notify(1, builder.build());  // 发送通知
        }
    }
}