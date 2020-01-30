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
        Log.i("test", "收到广播："+text);
        Toast.makeText(context,text,Toast.LENGTH_LONG).show();

        if(text.contains("后台运行")){
            //NotificationRun.NCSend(context,NotificationChannels.DEFAULT_ID,0,"我在后台","点击切换到前台！",true,null,1);

            NotificationRun.NCSend(context,NotificationChannels.CRITICAL_ID,0,"我在后台","点击切换到前台！",false,null,1,true);
        }

    }
}