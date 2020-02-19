package com.anysou.as_receiptnotice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * android使用NotificationListenerService监听通知栏消息三环节步骤：
 * 1、注册服务（在AndroidManifest.xml对service进行注册）
 * 2、继承实现NotificationListenerService。
 * 3、引导用户进行授权。
 *
 * 如何无缝监听安卓手机通知栏推送信息以及拒接来电 https://www.jianshu.com/p/cfba0d59ec1b?from=timeline
 *
 * NotificationListenerService的那些事儿  https://www.jianshu.com/p/981e7de2c7be
 *
 * Android 8.0中各种通知写法汇总  https://www.jianshu.com/p/6aec3656e274
 *
 * extends 是继承； implements 是接口
 * JAVA中不支持多重继承，继承只能继承一个类，但implements可以实现多个接口，用逗号分开就行了。
 *
 * Android中有五种数据存储方法：
 *  1）SQLite数据库、2）文件存储、
 *  3）Preference：以类似Map的键值对形式存储的，最适合用来保存用户个人设置之类的信息。
 *  4）ContentProvider：作用是为不同的应用之间数据共享，提供统一的接口。
 *  5）网络存储数据。
 *
 * preference:
 * getPreferences()：获取到作用域是本Activity的preference
 * getSharedPreferences()：获取到作用域是本应用程序的preference
 * getDefaultSharedPreferences()：获取到全局作用域的preference (包名一 样的可以取到)
 *
 * Context 上下文的表示的几种方法：
 * 1、this, 说明当前类是context的子类，一般是activity application等; Activity.this在Activity当中可以缩写为this。生命周期：对应于 activity application 。
 * 2、getContext() 获取的是 当前对象所在的 Context, Context通常翻译成上下文，可当成场景来理解。
 * 3、getApplicationContext() 取得的是当前app所使用的application，这在AndroidManifest中唯一指定。意味着，在当前app的任意位置使用这个函数得到的是同一个Context; 生命周期：整个应用，应用摧毁，它才摧毁。
 *    3A) getApplication(): andorid 开发中共享全局数据;
 * 4、getBaseContext()： 返回由构造函数指定或setBaseContext()设置的上下文.
 *
 *
 * */

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName(); //获取 “MainActivity”
    //private static final String TAG = "MainActivity";  //设置标签
    private Toolbar myToolbar;                 //工具导航栏
    private ViewPager2 viewpage;               //分片页面容器切换滚动
    private AutoCompleteTextView posturl;      //随笔提示文本组件
    private SharedPreferences sp ;             //轻量级的xml存储类
    private int Lid = 0;
    private String urlsample = "http://anypay.wang/anypay/1/";

//    private LocalBroadcastReceiver localReceiver = new LocalBroadcastReceiver();  //本地广播接收器
//    private LocalBroadcastManager localBroadcastManager = null;   //本地广播管理器

    // 此句在 onCreate 前面先执行，这里用来实现语言切换
    @Override
    protected void attachBaseContext(Context newBase) {
        //MainApplication.getCMSint("attachBaseContext");
        Log.i(MainApplication.getCMS(true),"该方法首句");

        Context context = LanContextWrapper.wrap(newBase);  //定义语言环境
        super.attachBaseContext(context);
    }

    /* Bundle savedInstanceState 捆绑保存状态：
    出现用户按到home键，退出了界面，或者android系统意外回收了应用的进程，在这种情况下，
    使用Bundle savedInstanceState就可以再次打开应用的时候恢复到原来的模样 */
    @Override  //@Override 重写父类方法
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(MainApplication.getCMS(true),"该方法首句");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //隐藏标题栏
        setContentView(R.layout.activity_main);

        Lid = MainApplication.LANGUAGEID;
        initView();            // 初始化所有的组件及数据
        posturlSuggestion();   // 实现：光标点上去就显示一组默认的下拉数据。

        //订阅 消息框架 “key” 的值变化
        LiveEventBus.get("key",String.class).observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) { //获取"key" 变化的新值
                Log.i("test",s);

                if(s.contains("SetNFChannel=")){
                    gotoSetNFChannel(s.split("=")[1]);
                } else {
                    sendToast(s);           // 吐司
                    sendLocalBroadcast(s);  // 发本地广播
                }
            }
        });

        //sendLocalBroadcast("主程序启动成功！");

        //现在部分手机(或大部分或只是个案)安装新APP后会默认不开启“锁屏通知权限” ，让客户手动去打开权限他还不乐意，一直找如何去动态申请权限，最后发现其实不需要申请权限，只需要几句话就好
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED //锁屏状态下显示
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD    //解锁
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON      //保持屏幕长亮
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);    //打开屏幕
    }


    //初始化所有的组件及数据
    private void initView() {
        //第一个参数（name）用于指定文件的名称，若指定的文件不存在则建一个；第二个参数（mode）用于指定操作模式，MODE_PRIVATE 该配置文件只能被自己的应用程序访问。
        sp = getSharedPreferences(MainApplication.SP_NAME, Context.MODE_PRIVATE);

        // 工具导航栏
        myToolbar= (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);  //ToolBar控件替代ActionBar控件

        // 随笔提示文本组件
        posturl = (AutoCompleteTextView) findViewById(R.id.posturl);
        if(getPostUrl()!=null)
            posturl.setHint(getPostUrl());
        else
            posturl.setHint(urlsample);

        // 分片页面容器切换滚动
        viewpage = findViewById(R.id.viewpager);
        // HomeFragmentsAdapter 适配器; 继承 FragmentStateAdapter（滑过后会保存当前界面，以及下一个界面和上一个界面（如果有），最多保存3个，其他会被销毁掉）
        FragmentsAdapterHome viewpageadapter = new FragmentsAdapterHome(this);
        viewpage.setAdapter(viewpageadapter);
    }

    // 设置 提交地址
    public void ButtonSetPostUrl(View view) {
        Log.i(MainApplication.getCMS(true),"该方法首句");
        //MainApplication.getCMSint("ButtonSetPostUrl");

        String posturlstr = ""+posturl.getText().toString();
        if(posturlstr==null || posturlstr==""){
            Toast.makeText(this,MainApplication.LANGUAGEID+"",Toast.LENGTH_LONG).show();
        } else {
            posturl.setHint(null);
            setPostUrl(posturlstr);
        }
    }

    // 写入设置
    private void setPostUrl(String posturlstr) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(MainApplication.POSTURL,posturlstr);  //通过editor对象写入数据
        edit.apply();  //提交数据存入到xml文件中
        Toast.makeText(getApplicationContext(), this.getText(R.string.text_SETURL)+posturlstr,Toast.LENGTH_SHORT).show();
    }

    // 读取设置的posturl
    private String getPostUrl(){
        String posturlpath;
        posturlpath = sp.getString(MainApplication.POSTURL, "");
        if (posturlpath==null || posturlpath=="")
            return null;
        else
            return posturlpath;
    }

    // 实现：光标点上去就显示一组默认的下拉数据。
    private void posturlSuggestion(){
        String[] str = new String[2];
        str[0] = urlsample;
        str[1] = getPostUrl();
        posturl.setThreshold(0);  //设置用户至少输入几个字符才会显示提示。
        //（上面设置0的目的是想默认点上去时需要显示一组默认的下拉数据，但是默认的AutoCompleteTextView是实现不了的， 因为setThreshold方法最小值是1，就算你设的值为0，也会自动改成1的。）
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,str);
        posturl.setAdapter(adapter);
        //设置一个光标改变监听事件（该监听事件就是为了实现：默认点上去时需要显示一组默认的下拉数据）
        posturl.setOnFocusChangeListener (new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                AutoCompleteTextView view = (AutoCompleteTextView) v;
                if (hasFocus) {
                    view.showDropDown ();  //显示下拉框
                }
            }
        });
        Toast.makeText(getApplicationContext(), str[0],Toast.LENGTH_SHORT).show();
    }

    // 浮动按键 调用打开 LogCAT 日志界面
    public void ButtonCallLogCAT(View view) {
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)  //LynxView中显示的最大跟踪数
                .setTextSizeInPx(12)       //用于在LynxView中呈现字体大小PX
                .setSamplingRate(200)      //从应用程序日志中读取的采样率
                .setFilter("NLService");   //设置过滤
        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
    }


    //==============================  通知栏监听服务 ==============================
    @Override
    protected void onResume() {
        super.onResume();
        checkNotificationService();
    }

    // 通知服务获取及开启功能
    private void checkNotificationService() {
        if (!isNotificationServiceEnable()) {
            // 对话框
            new AlertDialog.Builder(this).setTitle(this.getText(R.string.text_NSSQTS_1)).setMessage(this.getText(R.string.text_NSSQTS_2)).setPositiveButton(this.getText(R.string.text_NSSQTS_3), new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
                {
                    gotoNotificationAccessSetting();
                }
            }).create().show();
        } else {
            //Toast.makeText(this,L_NSOK[Lid],Toast.LENGTH_SHORT).show();
            Toast.makeText(this,this.getString(R.string.text_NSOK),Toast.LENGTH_SHORT).show();
            //finish();
        }
    }

    // 判断是否已授权获取 通知服务监听
    private boolean isNotificationServiceEnable() {
        try{  //contains判断是否是包含关系; 具体监听功能的包里面；是否有本APP的包名 getPackageName()
            return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
        }catch (Exception e) {
            try{
                boolean enable = false;
                String packageName = getPackageName();
                String flat= Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
                if (flat != null) { enable = flat.contains(packageName);  }
                return enable;
            } catch (Exception ex){
                ex.printStackTrace();
                Toast.makeText(this,"您的手机无法判断是否获取了“通知栏权限”！",Toast.LENGTH_LONG).show();
                return false;
            }
        }
    }

    //直接跳转通知授权界面
    private void gotoNotificationAccessSetting() {
        try {
            //android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS是API 22才加入到Settings里,  Android 5.1  22	LOLLIPOP_MR1
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //表示在新的一个堆栈开启设置界面。（也可以不使用，默认为原堆栈顺序打开设置，通过返回到系统原界面）
            startActivity(intent);
        } catch(Exception e) {
            try {
                ApplicationInfo appInfo = getApplicationInfo();
                String pkg = getPackageName();  //包名
                int uid = appInfo.uid;                  //UID
                Intent intent = new Intent();
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    // 这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("android.provider.extra.APP_PACKAGE", pkg);
                    intent.putExtra("android.provider.extra.CHANNEL_ID",uid);
                } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                    // //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("app_package", pkg);
                    intent.putExtra("app_uid", uid);
                } else {
                    // 其他 Android 4.4	19	KITKAT
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", pkg, null));
                }
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //表示在新的一个堆栈开启设置界面。（也可以不使用，默认为原堆栈顺序打开设置，通过返回到系统原界面）
                startActivity(intent);
            } catch(Exception ex) {
                //ex.printStackTrace();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                Toast.makeText(this,"您的手机无法直接调用“获取通知栏权限的系统设置功能”，请在设置界面中查找并设置！",Toast.LENGTH_LONG).show();
            }
        }
    }


    //===== 打开设置通知的渠道界面 ==
    private void gotoSetNFChannel(String ChannelID){
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        intent.putExtra(Settings.EXTRA_CHANNEL_ID, ChannelID);
        startActivity(intent);
    }


    //===========================   菜单 ===========================================

    @Override  //启动时建立 菜单（菜单布局对应在res/menu/目录下）
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);  //对应：res/menu/main.xml
        return true;
    }

    @Override  // 点击菜单对应的操作
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettingActivity();
                return true;
            case R.id.language_en:
                //Toast.makeText(this,"en",Toast.LENGTH_SHORT).show();
                changeEnglish();
                return true;
            case R.id.language_cn:
                //Toast.makeText(this,"cn",Toast.LENGTH_SHORT).show();
                changeChinese();
                return true;
            case R.id.language_hk:
                //Toast.makeText(this,"hk",Toast.LENGTH_SHORT).show();
                changeChinese_HK();
                return true;
            default: //注意这两句必须要有
                return super.onOptionsItemSelected(item);

        }
    }
    // 打开设置界面
    private void openSettingActivity(){
        Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
        startActivity(intent);
    }


    //======================= 设置修改语言 =========================================
    public void changeChinese() {
        SharedPreferences sharedPreferences=getSharedPreferences(MainApplication.SP_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putString(MainApplication.LANGUAGE, LanContextWrapper.LANG_CN).apply();
        rebot();
    }
    public void changeEnglish() {
        SharedPreferences sharedPreferences=getSharedPreferences(MainApplication.SP_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putString(MainApplication.LANGUAGE, LanContextWrapper.LANG_EN).apply();
        rebot();
    }
    public void changeChinese_HK() {
        SharedPreferences sharedPreferences=getSharedPreferences(MainApplication.SP_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putString(MainApplication.LANGUAGE, LanContextWrapper.LANG_HK).apply();
        rebot();
    }
    // 重新启动
    private void rebot() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            //overridePendingTransition(R.anim.anim_change_lang_enter, R.anim.anim_change_lang_exit);
            finish();
        }else{
            recreate();
        }
    }


    // ================ 发本地广播 ===================================
    private void sendLocalBroadcast(String msg) {
        Intent intent = new Intent(getPackageName());
        intent.putExtra("text", msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //localBroadcastManager.unregisterReceiver(localReceiver); //注销本地广播
    }


    private void sendToast(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    //判断是否为模拟器
    public boolean isEmulator() {
        /*
        <!-- 添加拨号权限,但安卓6.0以后该句无效果了。采用了动态权限控制-->
        <uses-permission android:name="android.permission.CALL_PHONE"/>
         */
        //动态的请求权限
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CALL_PHONE
        }, 0x11); //0x11是请求码，可以在回调中获取
        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(url));
        //startActivity(intent);
        // 是否可以处理跳转到拨号的 Intent
        boolean isPhone = intent.resolveActivity(this.getPackageManager()) != null;
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("MuMu")
                || Build.MODEL.contains("virtual")
                || Build.SERIAL.equalsIgnoreCase("android")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT)
                || ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE))
                .getNetworkOperatorName().toLowerCase().equals("android")
                || !isPhone;
    }


}




