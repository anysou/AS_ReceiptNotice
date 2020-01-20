package com.anysou.as_receiptnotice;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.github.pedrovgs.lynx.LynxActivity;
import com.github.pedrovgs.lynx.LynxConfig;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
 * extends 是继承； implements 是接口
 * JAVA中不支持多重继承，继承只能继承一个类，但implements可以实现多个接口，用逗号分开就行了。
 * */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";  //设置标签
    private Toolbar myToolbar;                 //工具导航栏
    private ViewPager2 viewpage;               //分片页面容器切换滚动
//    private Button btnsetposturl;              //按键 设置POST URL
//    private FloatingActionButton btnshowlog;   //浮动按键；进入LogCAT
    private AutoCompleteTextView posturl;      //随笔提示文本组件
    private SharedPreferences sp ;             //轻量级的xml存储类
    private String urlsample = "http://anypay.wang/anypay/1/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();            // 初始化所有的组件及数据
        posturlSuggestion();   // 实现：光标点上去就显示一组默认的下拉数据。
    }

    //初始化所有的组件及数据
    private void initView() {
        //第一个参数（name）用于指定文件的名称，若指定的文件不存在则创建一个；第二个参数（mode）用于指定操作模式，默认操作模式为MODE_PRIVATE。
        sp = getSharedPreferences("url", Context.MODE_PRIVATE);

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


//        btnsetposturl=(Button) findViewById(R.id.btnsetposturl);
//        btnsetposturl.setOnClickListener(this);
//
//        btnshowlog=(FloatingActionButton) findViewById(R.id.floatingshowlog);
//        btnshowlog.setOnClickListener(this);

    }

    // 设置 提交地址
    public void SetPostUrl(View view) {
        posturl.setHint(null);
        setPostUrl();
    }

    // 写入设置
    private void setPostUrl() {
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("posturl",posturl.getText().toString());  //通过editor对象写入数据
        edit.apply();  //提交数据存入到xml文件中
        Toast.makeText(getApplicationContext(), "已经设置posturl为："+posturl.getText().toString(),Toast.LENGTH_SHORT).show();
    }

    // 读取设置的posturl
    private String getPostUrl(){
        String posturlpath;
        posturlpath = sp.getString("posturl", "");
        if (posturlpath==null)
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
    public void CallLogCAT(View view) {
        LynxConfig lynxConfig = new LynxConfig();
        lynxConfig.setMaxNumberOfTracesToShow(4000)  //LynxView中显示的最大跟踪数
                .setTextSizeInPx(12)       //用于在LynxView中呈现字体大小PX
                .setSamplingRate(200)      //从应用程序日志中读取的采样率
                .setFilter("NLService");   //设置过滤
        Intent lynxActivityIntent = LynxActivity.getIntent(this, lynxConfig);
        startActivity(lynxActivityIntent);
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
            default: //注意这两句必须要有
                return super.onOptionsItemSelected(item);

        }
    }

    // 打开设置界面
    private void openSettingActivity(){
        Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
        startActivity(intent);
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
            new AlertDialog.Builder(this).setTitle("温馨提示").setMessage("当前您未授权本系统读取通知栏权限，请前往授权后再继续操作！").setPositiveButton("前往授权", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
                {
                    gotoNotificationAccessSetting();
                }
            }).create().show();
        } else {
            Toast.makeText(this,"通知栏监听服务已开启！",Toast.LENGTH_SHORT).show();
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





    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
