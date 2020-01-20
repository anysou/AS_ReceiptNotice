package com.anysou.as_receiptnotice;

import android.app.Application;
import android.content.Context;

import com.anysou.aslogger.ASLogApplication;
import com.anysou.aslogger.ASLogIConfig;
import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * 设置全局变量、全局方法、启动监测服务、初始化TLog(日志工具)、设置LiveEventBus(消息总线框架)
 *
 * Andoird中LiveEventBus的使用——用LiveEventBus替代RxBus、EventBus https://blog.csdn.net/qq_43143981/article/details/101678528
 *
 * */

public class MainApplication extends Application {

    public static Context mContext;  //全局变量 APP的上下文
    public static Context getAppContext(){
        return mContext;
    }  //全局方法，获取 APP的上下文
    //全局方法，获取 调用的类名和方法名
    public static String getClassMethodStr() {
        //StackTrace(堆栈轨迹)存放的就是方法调用栈的信息，每次调用一个方法会产生一个方法栈，当前方法调用另外一个方法时会使用栈将当前方法的现场信息保存在此方法栈当中，获取这个栈就可以得到方法调用的详细过程。
        StackTraceElement[] elements = Thread.currentThread().getStackTrace(); //获取当前线程状态

        return elements[3].getClassName() + "__" + elements[3].getMethodName();
    }


    @Override
    public void onCreate() {
        super.onCreate();

        initASLogConfig();  //初始化AS_logger日志管理工具库,用来记录检测到的收款信息
        setSomeGlobal();    //设置一些全局变量
        setMessageBus();    //设置LiveEventBus消息消息总线框架
    }

    private void initASLogConfig() {
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

    private void setSomeGlobal(){
        mContext = getApplicationContext();
    }

    public void setMessageBus(){
        /**LiveEventBus，一款具有生命周期感知能力的消息总线框架
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
         *
         * */
        LiveEventBus.config()
                .supportBroadcast(this)
                .lifecycleObserverAlwaysActive(true)
                .autoClear(false);
    }

}
