package com.anysou.as_receiptnotice;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 *  通知监听服务： 继承：NotificationListenerService， 接口：AsyncResponse, IDoPost, ActionStatusBarNotification
 *  1、注册通知监听服务，并设置权限；  2、启动本服务，等待通知。 3、获取通知权限的判断及打开通知权限设置页面；
 *
 *  Android利用NotificationListenerService实现消息盒子功能  https://blog.csdn.net/Vanswells/article/details/81033280
 *  NotificationListenerService的那些事儿 https://www.jianshu.com/p/981e7de2c7be?from=jiantop.com
 *
 *  全新的Android通知栏,已抛弃setLatestEventInfo,兼容高版本  https://github.com/linglongxin24/NotificationUtil
 * **/

public class NLService extends NotificationListenerService implements AsyncResponse, IDoPost, ActionStatusBarNotification {

    private String TAG="NLService";     // TAG
    private Context context=null;       // 上下文
    private String posturl=null;        // 提交POST地址
    private String getPostUrl(){        // 通过轻量存储读取POST地址
        SharedPreferences sp = getSharedPreferences("url", 0);
        this.posturl =sp.getString("posturl", "");
        if (posturl==null)
            return null;
        else
            return posturl;
    }


    @Override
    public void onListenerConnected() {
        //当连接成功时调用，一般在开启监听后会回调一次该方法
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override  //当收到一条消息时回调，sbn里面带有这条消息的具体信息
    public void onNotificationPosted(StatusBarNotification sbn) {
        // super.onNotificationPosted(sbn);
        // 这里只是获取了包名和通知提示信息，其他数据可根据需求取，注意空指针就行
        if(getPostUrl()==null)
            return;

        Notification notification = sbn.getNotification();
        if (notification == null) {
            return;
        }
        Bundle extras = notification.extras;
        if(extras==null)
            return;

        String pkg = sbn.getPackageName();  //获取发送通知的应用程序包名

        boolean clearable = sbn.isClearable(); //通知是否可被清除
        if (!clearable) { //如果是常驻的  不通知  只有真正的通知才显示
            LogUtil.debugLog("该通知是不可清除的，包名="+pkg);
            return;
        }

        /*
        int NFid = sbn.getId();                //获取通知id
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                String NFkey = sbn.getKey();   //获取通知的key
        }
        Long NFtime = sbn.getPostTime();       //通知的发送时间


        //====通知的具体内容
        String title = extras.getString(Notification.EXTRA_TITLE);               //通知title
        String content = extras.getString(Notification.EXTRA_TEXT);              //通知内容
        int smallIconId = extras.getInt(Notification.EXTRA_SMALL_ICON);          //通知小图标id
        Bitmap largeIcon =  extras.getParcelable(Notification.EXTRA_LARGE_ICON); //通知的大图标，注意和获取小图标的区别
        PendingIntent pendingIntent = sbn.getNotification().contentIntent;       //获取通知的PendingIntent
         */

        //推送 NotificationHandleFactory 委托处理
        NotificationHandle notihandle = new NotificationHandleFactory().getNotificationHandle(pkg,notification,this);
        // 是否为需要处理的APP类通知
        if(notihandle!=null){
            notihandle.setStatusBarNotification(sbn);  //传 sbn
            notihandle.setActionStatusbar(this);       //传 Action组件
            notihandle.printNotify();                  //打印通知信息
            notihandle.handleNotification();           //处理通知
            notihandle.removeNotification();           //移除通知
            return;
        }

        // 其他通知信息显示
        LogUtil.debugLog("-----------------");
        LogUtil.debugLog("接受到通知消息");
        LogUtil.debugLog("这是检测之外的其它通知");
        LogUtil.debugLog("包名是"+pkg);
        NotificationUtil.printNotify(notification);
        //printNotify(getNotitime(notification),getNotiTitle(extras),getNotiContent(extras));
        LogUtil.debugLog("**********************");
    }


    // 移除通知的具体操作（对应移除接口）
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void removeNotification(StatusBarNotification sbn){
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        if(preference.isRemoveNotification()){  //获取设置，是否推送后移除通知
            if (Build.VERSION.SDK_INT >=21)
                cancelNotification(sbn.getKey());
            else
                cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
            sendToast("receiptnotice移除了包名为"+sbn.getPackageName()+"的通知");
        }
    }

    @Override  //当移除一条消息的时候回调，sbn是被移除的消息
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (Build.VERSION.SDK_INT >19)
            super.onNotificationRemoved(sbn);
    }

    // 发本地广播
    private void sendBroadcast(String msg) {
        Intent intent = new Intent(getPackageName());
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // 发送给吐司
    private void sendToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    // 推送POST
    public void doPost(Map<String, String> params){
        if(this.posturl==null|params==null)
            return;
        LogUtil.debugLog("开始准备进行post");
        if(params.get("repeatnum")!=null){
            doPostTask(params,null);
            return;
        }

        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        PostMapFilter mapfilter = new PostMapFilter(preference,params,this.posturl);
        Map<String, String> recordmap = mapfilter.getLogMap();
        Map<String, String> postmap = mapfilter.getPostMap();
        doPostTask(postmap,recordmap);
    }
    // 推送的具体工作
    private void doPostTask(Map<String, String> postmap,Map<String, String> recordmap){
        PostTask mtask = new PostTask();
        String tasknum = RandomUtil.getRandomTaskNum();
        mtask.setRandomTaskNum(tasknum);
        mtask.setOnAsyncResponse(this);
        if(recordmap!=null)
            LogUtil.postRecordLog(tasknum,recordmap.toString());
        else
            LogUtil.postRecordLog(tasknum,postmap.toString());
        mtask.execute(postmap);
    }


    @Override
    public void onDataReceivedSuccess(String[] returnstr) {
        Log.d(TAG,"POST成功返回的数据");
        Log.d(TAG,returnstr[2]);
        LogUtil.postResultLog(returnstr[0],returnstr[1],returnstr[2]);
    }

    @Override
    public void onDataReceivedFailed(String[] returnstr,Map<String ,String> postedmap) {
        // TODO Auto-generated method stub
        Log.d(TAG,"POST失败");
        LogUtil.postResultLog(returnstr[0],returnstr[1],returnstr[2]);
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        if(preference.isPostRepeat()){
            String repeatlimit = preference.getPostRepeatNum();
            int limitnum = Integer.parseInt(repeatlimit);
            String repeatnumstr = postedmap.get("repeatnum");
            int repeatnum = Integer.parseInt(repeatnumstr);
            if(repeatnum<=limitnum)
                doPost(postedmap);
        }
    }

    // 获取通知的时间，并格式化
    private String getNotitime(Notification notification){
        long when=notification.when;
        Date date=new Date(when);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String notitime=format.format(date);
        return notitime;
    }

    // 获取通知的标题
    private String getNotiTitle(Bundle extras){
        String title=null;
        title = extras.getString(Notification.EXTRA_TITLE, "");
        return title;
    }

    // 获取通知的内容
    private String getNotiContent(Bundle extras){
        String content=null;
        content = extras.getString(Notification.EXTRA_TEXT, "");
        return content;
    }

    // 显示通知的时间、标题、内容
    private void printNotify(String notitime,String title,String content){
        Log.d(TAG,notitime);
        Log.d(TAG,title);
        Log.d(TAG,content);
    }
}
