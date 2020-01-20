package com.anysou.as_receiptnotice;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Process;
import android.os.Build;
import android.util.Log;
import android.os.PowerManager.WakeLock;
import android.os.PowerManager;
import io.socket.client.IO;
import io.socket.client.Socket;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.Random;
import java.lang.System;
import java.lang.Thread;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLSocketFactory;
import com.google.gson.Gson;
import io.socket.emitter.Emitter;
import java.util.Timer;
import java.util.TimerTask;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;
import okhttp3.ConnectionSpec;


/**
 * 在你的 Application 的 onCreate 中调用： startService(new Intent(this, NotificationCollectorMonitorService.class));
 * 同时记得在 AndroidManifest.xml 中注册服务： <service android:name=".NotificationCollectorMonitorService"/>
 */


public class NotificationCollectorMonitorService extends Service {

    /**
     * 注意：设置 TAG 的内容长度要 < 23
     */
    private static final String TAG = "NotifiCollectorMonitor";
    private Timer timer = null;                // 定时器
    private String echointerval = null;        // 时间间隔
    private TimerTask echotimertask =null;     // 时间任务
    private WakeLock wl = null;                // 休眠锁

    @Override
    public void onCreate() {
        super.onCreate();

        ensureCollectorRunning();  //确保通知监听服务组件运行中
        setWakelock();             //根据配置，设置唤醒锁，确保CPU不进入休眠状态（android.permission.WAKE_LOCK权限）
        startEchoTimer();          //根据配置，启动Socket.IO实现即时通讯（android.permission.INTERNET权限、io.socket:socket.io-client:1.0.0库）
    }

    //=================== 通过查询所有服务，确定NLService 通知监听服务是否启动，没启动则启动 ======================

    // 确保服务组件运行中
    private void ensureCollectorRunning() {
        // 获取要检测运行的 组件名称
        ComponentName collectorComponent = new ComponentName(this, /* 继承 NotificationListenerService*/ NLService.class);
        Log.v(TAG, "要确保运行的组件名称: " + collectorComponent);

        // 获取当前激活的服务对象
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean collectorRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE); // 列出所有服务
        if (runningServices == null ) {
            Log.w(TAG, "当前的服务列表为空");
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServices) {
            if (service.service.equals(collectorComponent)) {
                Log.w(TAG, "要确保运行的服务组件 - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount
                        + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")"));
                if (service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                    collectorRunning = true;
                }
            }
        }
        if (collectorRunning) {
            Log.d(TAG, "确保运行的服务组件运行中。。。");
            return;
        }
        Log.d(TAG, "确保运行的服务组件尚未运行, 重启中。。。");
        toggleNotificationListenerService();
    }
    // 重启服务组件
    private void toggleNotificationListenerService() {
        //Log.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ NLService.class);
        PackageManager pm = getPackageManager();
        /**componentName：组件名称（本例是服务组件。如果是app的mainActivity组件，可实现程序图标隐藏）
           newState：组件新的状态，可以设置三个值，分别是如下： 不可用状态：COMPONENT_ENABLED_STATE_DISABLED 
                   可用状态：COMPONENT_ENABLED_STATE_ENABLED ； 默认状态：COMPONENT_ENABLED_STATE_DEFAULT 
                   flags:行为标签，值可以是DONT_KILL_APP或者0。 0说明杀死包含该组件的app
         **/
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }


    //=================== 根据配置，设置唤醒锁，确保CPU不进入休眠状态 ============================================

    // 设置
    private void setWakelock() {
        //PreferenceUtil 是自定义的配置管理类
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        if(preference.isWakelock())  //读取设置
            obtainWakelock();    //获得锁
    }
    // 设置并获得锁
    @SuppressLint("InvalidWakeLockTag")
    private void  obtainWakelock() {
        /**电源管理架构:
         * https://blog.csdn.net/weixin_37730482/article/details/80108786
         * https://www.cnblogs.com/onelikeone/p/9521761.html
         *
         * PARTIAL_WAKE_LOCK:保持CPU 运转，屏幕和键盘灯有可能是关闭的。
         * SCREEN_DIM_WAKE_LOCK：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯
         * 过期:SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯 ,WindowManager.LayoutParams#FLAG_KEEP_SCREEN_ON
         * FULL_WAKE_LOCK：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
         *
         * 下面这俩要和上面的4个配合,才能使用
         * ACQUIRE_CAUSES_WAKEUP：强制使屏幕亮起，这种锁主要针对一些必须通知用户的操作.
         * ON_AFTER_RELEASE：当锁被释放时，保持屏幕亮起一段时间
         *
         * **/
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        //wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        //.PARTIAL_WAKE_LOCK：保证CPU保持高性能运行，而屏幕和键盘背光（也可能是触摸按键的背光）关闭。一般情况下都会使用这个WakeLock。
        //wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,TAG);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"receiptnoticewakelock");
        if(wl!=null)
            wl.acquire(); //获得
    }
    //释放锁
    private void releaseWakelock() {
        if(wl!=null)
            wl.release();  //释放
        else
            return;
    }


    //=================== 根据配置（是否使用Echo服务、服务器地址、通信间隔时间），启动Socket.IO实现即时通讯 =====================
    // 开始启动Echo定时器
    private void startEchoTimer(){
        PreferenceUtil preference=new PreferenceUtil(getBaseContext());
        String interval = preference.getEchoInterval(); // 获取配置文件中的 时间间隔
        this.echointerval = (!interval.equals("") ?  interval:getDefaultEchoInterval()); // 如果配置没有，取默认的
        this.echotimertask = returnEchoTimerTask();
        this.timer=new Timer();
        int intervalmilliseconds = Integer.parseInt(this.echointerval)*1000; //转为毫秒
        LogUtil.infoLog("socket.io 通信间隔时间（ms）:"+intervalmilliseconds);
        //schedule（task，time，period） task-所要安排执行的任务 time-首次执行任务的时间 period-执行一次task的时间间隔，单位毫秒
        //作用：时间等于或者超过time首次执行task，之后每隔period毫秒重复执行一次任务
        timer.schedule(echotimertask,5*1000,intervalmilliseconds); //5秒后执行、之后每intervalmilliseconds执行一次
    }
    // 默认的时间间隔
    private String getDefaultEchoInterval(){
        if (Build.VERSION.SDK_INT >= 22 )
            return  "300";
        else
            return  "100";
    }
    // 定时要做的任务
    private TimerTask returnEchoTimerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                if(!isIntervalMatchPreference()){
                    restartEchoTimer();
                    return;
                }
                LogUtil.debugLog("socketio 定时到，开始启动服务任务");
                boolean flag= echoServer();
                if(!flag)
                    LogUtil.debugLog("socketio定时服务没有运行");
            }
        };
    }
    private boolean isIntervalMatchPreference(){
        PreferenceUtil preference = new PreferenceUtil(getBaseContext());
        String interval = preference.getEchoInterval();
        if(interval.equals(""))
            return true;
        if(interval.equals(this.echointerval))
            return true;
        return false;
    }
    // 重新启动定时
    private void restartEchoTimer(){
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        if (echotimertask != null) {
            echotimertask.cancel();
            echotimertask = null;
        }
        LogUtil.debugLog("重新启动定时任务");
        startEchoTimer();
    }
    // 启动的服务
    private boolean echoServer(){
        PreferenceUtil preference=new PreferenceUtil(getBaseContext());
        Gson gson = new Gson();  //Gson是一个Java库，它可以用来把Java对象转换为JSON表达式，也可以反过来把JSON字符串转换成与之相同的Java对象
        // 需要Echo 且 Echo服务器存在
        if(preference.isEcho() && (preference.getEchoServer()!=null)){
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = format.format(date);
            DeviceBean device = new DeviceBean();
            String deviceid = preference.getDeviceid();  //获取设置的设备ID
            deviceid=(!deviceid.equals("") ? deviceid:DeviceInfoUtil.getUniquePsuedoID()); //没有就用系统的UUID
            device.setDeviceid(deviceid);
            device.setTime(time);
            LogUtil.debugLog("开始启动 socketio 连接");
            echoServerBySocketio(preference.getEchoServer(),gson.toJson(device)); //发送：设备ID、当前连接时间
            LogUtil.debugLog(gson.toJson(device));
            return true;
        }
        else
            return false;
    }
    // 要发生的设备信息类
    public class DeviceBean{
        public String deviceid;
        public String connectedtime;
        public void setDeviceid(String deviceid){  //设备ID
            this.deviceid = deviceid;
        }
        public void setTime(String time){
            this.connectedtime = time;
        }
    }
    // 启动 socketio 连接
    private boolean echoServerBySocketio(String echourl,String echojson){
        Socket mSocket= EchoSocket.getInstance(echourl);  //获取Socket实例
        mSocket.connect();
        mSocket.emit("echo",echojson);  //表示发送了一个action命令
        mSocket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                LogUtil.infoLog("socket 连接失败,将在5秒钟后重试");
                try{
                    Thread.sleep(5000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                echoServer();
            }
        });
        return true;
    }
    // Echo Socket 服务
    public static class EchoSocket{
        private static Socket instance1=null;
        private static Socket instance2=null;
        private static Socket instance3=null;
        private static final int maxCount = 3;
        private EchoSocket(){
        }
        public static Socket getThisInstance(int i){
            if(i==1)
                return EchoSocket.instance1;
            if(i==2)
                return EchoSocket.instance2;
            if(i==3)
                return EchoSocket.instance3;
            else
                return null;
        }
        public static Socket getInstance(String socketserverurl){
            Random random = new Random();
            int current = random.nextInt(maxCount)+1;
            if(getThisInstance(current)==null){
                synchronized(EchoSocket.class){
                    if(current==1)
                        instance1=getIOSocket(socketserverurl);
                    if(current==2)
                        instance2=getIOSocket(socketserverurl);
                    if(current==3)
                        instance3=getIOSocket(socketserverurl);
                }
            }
            return getThisInstance(current);
        }
        // 连接Socket服务器
        public static Socket getIOSocket(String socketserverurl){
            try{
                if (Build.VERSION.SDK_INT >= 22 ){
                    return IO.socket(socketserverurl);
                }
                else{
                    SSLSocketFactory factory = new SSLSocketFactoryCompat();
                    ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .build();
                    List<ConnectionSpec> specs = new ArrayList<>();
                    specs.add(cs);
                    specs.add(ConnectionSpec.COMPATIBLE_TLS);
                    specs.add(ConnectionSpec.CLEARTEXT);
                    OkHttpClient client = new OkHttpClient.Builder()
                            .sslSocketFactory(factory)
                            .connectionSpecs(specs)
                            .build();
                    IO.setDefaultOkHttpWebSocketFactory(client);
                    IO.setDefaultOkHttpCallFactory(client);
                    // set as an option
                    IO.Options opts = new IO.Options();
                    opts.callFactory = client;
                    opts.webSocketFactory = client;
                    return IO.socket(socketserverurl, opts);
                }
            }catch(URISyntaxException e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                LogUtil.debugLog(sw.toString());
                return null;
            }catch (KeyManagementException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

