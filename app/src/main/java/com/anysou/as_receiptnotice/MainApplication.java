package com.anysou.as_receiptnotice;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.anysou.aslogger.ASLogApplication;
import com.anysou.aslogger.ASLogIConfig;
import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * 设置全局变量、全局方法、启动监测服务（通知监听服务、根据配置获得唤醒锁、根据配置进行Echo实时通信）、初始化TLog(日志工具)、设置LiveEventBus(消息事件总线框架)、监听判断是否进入后台
 */

public class MainApplication extends Application {

    public static Context mContext;  //全局变量 APP的上下文
    public static Context getAppContext(){
        return mContext;
    } //全局方法，获取 APP的上下文
    public static final String SP_NAME = "AS_RN";      //定义本APP轻量存储的文件名
    public static final String POSTURL = "posturl";    //定义存储POST地址的KEY
    public static final String LANGUAGE = "language";  //定义存储语言的KEY
    public static int LANGUAGEID = 0;                  //语言ID 0=英语 1=简体 2=繁体 [可用于一些不语言的数组索引序号]
    public static int activityCount = 0;               //Activity开启的数量

    public static Boolean istest = true;   //是否为测试

    // 全局方法，获取 调用的类名和方法名(isTAG 是不是用做TAG) 注意：设置 TAG 的内容长度要 < 23
    public static String getCMS(boolean isTAG) {
        //StackTrace(堆栈轨迹)存放的就是方法调用栈的信息，每次调用一个方法会产生一个方法栈，当前方法调用另外一个方法时会使用栈将当前方法的现场信息保存在此方法栈当中，获取这个栈就可以得到方法调用的详细过程。
        StackTraceElement[] elements = Thread.currentThread().getStackTrace(); //获取当前线程状态
        int jzs = 15;
        if(elements.length<=jzs) jzs = 3;
        else jzs = 3;  //此数字,根据具体而定 可通过getCMSint测试
        String[] ClassNameArr = elements[jzs].getClassName().split("\\.");
        String ClassName = ClassNameArr[ClassNameArr.length-1];
        String MethodName = elements[jzs].getMethodName();
        if(istest){
            Log.d("test", ""+elements.length);
            Log.d("test", "_" + ClassName + "_" + MethodName);
        }
        String reMsg = "_" + ClassName + "_" + MethodName;
        if(isTAG && reMsg.length()>=23){  //注意：设置 TAG 的内容长度要 < 23
            int ClassNameLen = (ClassName.length()<=10) ? ClassName.length() : 10;
            int MethodNameLen = (MethodName.length()<=10) ? MethodName.length() : 10;
            reMsg = "_"+ClassName.substring(0,ClassNameLen)+"_"+MethodName.substring(0,MethodNameLen);
        }
        return reMsg;
    }
    // 用于开发测试时，获取getCMS函数里的jzs值
    public static void getCMSint(String MethodName){
        StackTraceElement[] elements = Thread.currentThread().getStackTrace(); //获取当前线程状态
        int jzs = 0;
        for(int i=1;i<elements.length; i++) {
            try{
                Log.d("test", elements[i].getClassName()+"_"+elements[i].getMethodName());
                if(elements[i].getMethodName().contains(MethodName)){
                    jzs = i;
                    Log.d("test", "CMS int = " + i);
                    break;
                }
            }
            catch (Exception e){ }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        setSomeGlobal();    //设置一些全局变量
        startNotificationService();  //启动通知监听服务、根据配置获的唤醒锁、根据配置进行Echo实时通信
        initASLogConfig();  //初始化日志管理工具库,用来记录检测到的收款信息
        setMessageBus();    //设置LiveEventBus消息事件总线框架（Android组件间通信工具）
        initActivityLife(); //通过接口监听所有Activity的生命周期状态，实现判断是否进入后台
    }


    private void setSomeGlobal(){
        Log.d(getCMS(true),"启动：设置一些全局变量");

        mContext = getApplicationContext();
    }


    private void startNotificationService(){
        Log.d(getCMS(true),"启动：通知监听服务、根据配置获得唤醒锁、根据配置进行Echo实时通信");

        startService(new Intent(this, NotificationCollectorMonitorService.class));
    }


    private void initASLogConfig() {
        Log.d(getCMS(true),"启动：初始化AS_Logger日志管理工具库,用来记录检测到的收款信息");

        ASLogApplication.init(this);
        ASLogIConfig.getInstance()
                .setShowLog(true)     //是否在logcat中显示log,默认不显示。
                .setWriteLog(true)    //是否在文件中记录，默认不记录。
                .setFileSize(100000)  //日志文件的大小，默认0.1M,以bytes为单位。
                .setSQLLen(100)       //设置SQLite数据库的数据最大条数。默认100条。
                .setSaveSQL(false)     //设置为file文本存储记录方式，true=SQLite数据库。
                .setAutoUpdate(true)  //设置为逐步替换更新模式。（file文本存储记录方式才有效）
                .setTag("GoFileService");  //logcat日志过滤tag。
    }



    //==============  设置 LiveEventBus 消息事件总线框架（Android组件间通信工具） =========
    public void setMessageBus(){
        Log.d(getCMS(true),"启动：设置LiveEventBus消息事件总线框架");

        /**LiveEventBus，一款具有生命周期感知能力的消息事件总线框架（Android组件间通信工具）
         * Andoird中LiveEventBus的使用——用LiveEventBus替代RxBus、EventBus https://blog.csdn.net/qq_43143981/article/details/101678528
         * 消息总线，基于LiveData，具有生命周期感知能力，支持Sticky，支持AndroidX，支持跨进程，支持跨APP
         * https://github.com/JeremyLiao/LiveEventBus
         *
         * 1、build.gradle 中引用  implementation 'com.jeremyliao:live-event-bus-x:1.5.7'
         * 2、初始化 LiveEventBus
         *   1）supportBroadcast 配置支持跨进程、跨APP通信
         *   2）配置 lifecycleObserverAlwaysActive 接收消息的模式（默认值true）：
         *      true：整个生命周期（从onCreate到onDestroy）都可以实时收到消息
         *      false：激活状态（Started）可以实时收到消息，非激活状态（Stoped）无法实时收到消息，需等到Activity重新变成激活状态，方可收到消息
         *   3) autoClear 配置在没有Observer关联的时候是否自动清除LiveEvent以释放内存（默认值false）
         * 3、发送消息：
         *    LiveEventBus.get("key").post("value");  //发送一条即时消息
         *    LiveEventBus.get("key").postDelay("value",3000);  //发送一条延时消息 3秒跳转
         * 4、接受消息，注册一个订阅，在需要接受消息的地方
         *   LiveEventBus.get("key",String.class).observe(this, new Observer<String>() {
         *      @Override
         *      public void onChanged(@Nullable String s) {
         *              Log.i(TAG,s);
         *      }
         *   });
         * */
        LiveEventBus.config()
                .supportBroadcast(this)
                .lifecycleObserverAlwaysActive(true)
                .autoClear(false);
    }


    //==============  通过接口监听所有Activity的生命周期状态，实现判断是否进入后台 ============================
    //Android--判断App处于前台还是后台的方案  https://blog.csdn.net/u011386173/article/details/79095757
    private void initActivityLife(){
        this.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }
            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                if (activityCount == 0) {
                    //app回到前台
                    Log.i("test","回到前台");
                }
                activityCount++;
            }
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }
            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }
            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                MainApplication.activityCount--;
                if (MainApplication.activityCount == 0) {
                    //Toast.makeText(getApplicationContext(),"后台运行中！",Toast.LENGTH_LONG).show();  //方法一：直接吐司
                    //sendLocalBroadcast("后台运行中，点击通知切换到前台！"); //方法二： 发本地广播；接收广播后吐司；并发布通知
                    LiveEventBus.get("key").post("APP后台运行中，点击通知切换到前台！");  //方法二： 发消息；接收广播后吐司；并发布通知
                }
            }
            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }
            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    // 发本地广播
    private void sendLocalBroadcast(String msg) {
        Intent intent = new Intent(getPackageName());
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
