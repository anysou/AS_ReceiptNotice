
package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通知信息各值 提取类
 * */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)  //KitKat	4.4	2013年7月24日	API level 19
public class NotificationUtil {

        // 通知的时间
        private static String getNotitime(Notification notification){
                long when=notification.when;
                Date date=new Date(when);
                SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String notitime=format.format(date);
                return notitime;
        }

        // 获取通知标题
        private static String getNotiTitle(Bundle extras){
                String title=null;
                title = extras.getString(Notification.EXTRA_TITLE, "");
                return title;
        }

        // 获取通知内容
        private static String getNotiContent(Bundle extras){
                String content=null;
                content = extras.getString(Notification.EXTRA_TEXT, "");
                return content;
        }

        // 打印：通知时间、标题、内容
//        public static void printNotify(Notification notification){
//                LogUtil.debugLog("时间："+getNotitime(notification)+"\n标题："+getNotiTitle(notification.extras)+"\n内容："+getNotiContent(notification.extras));
//        }

        // 打印：通知时间、标题、内容
        public static void printNotify(Notification notification){
                LogUtil.debugLog("时间："+getNotitime(notification));
                LogUtil.debugLog("标题："+getNotiTitle(notification.extras));
                LogUtil.debugLog("内容："+getNotiContent(notification.extras));
        }

}
