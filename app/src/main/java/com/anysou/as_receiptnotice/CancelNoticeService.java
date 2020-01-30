package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/***
 * 如果感觉前台服务的通知不喜欢，可用这个服务来移除该通知，退出前台服务到后台了。（用于隐藏前台服务的通知，不常用）
 */

public class CancelNoticeService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {


            // 将本服务替换掉前台服务的通知 (通知ID要相同)
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            startForeground(NotificationCollectorMonitorService.NOTICE_ID, builder.build());

            // 延迟1s
            SystemClock.sleep(1000);
            // 取消自己的 CancelNoticeService的 前台
            stopForeground(true);
            // 移除此时可以移除 DaemonService弹出的通知，指定删除对应的通知ID
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.cancel(NotificationCollectorMonitorService.NOTICE_ID);
            // 退出前台服务了
            MainApplication.NCRun = false;
            // 任务完成，终止自己
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }
}