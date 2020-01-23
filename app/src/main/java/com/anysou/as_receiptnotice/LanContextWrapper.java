package com.anysou.as_receiptnotice;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;

import java.util.Locale;

/**
 * 自定义用于资源适配的ContextWrapper, 实现App国际化之动态切换多语言
 * https://blog.csdn.net/CrazyMo_/article/details/82151113
 *
 * Context是上下文抽象类，ContextImpl是具体功能实现类，ContextWrapper是代理类。
 *
 * ContextWrapper 代理Context的实现，将其所有调用简单地委托给另一个Context。可以作为子类去修改行为而不更改原始的Context。
 * **/
public class LanContextWrapper extends ContextWrapper {

    public LanContextWrapper(Context ctx) {
        super(ctx);
    }

    public static final String LANG_HK = "hk";
    public static final String LANG_CN = "cn";
    public static final String LANG_EN = "en";

    public static ContextWrapper wrap(Context context) {
        Locale newLocale;
        SharedPreferences sharedPreferences = context.getSharedPreferences(MainApplication.SP_NAME, MODE_PRIVATE);
        String lanTag = sharedPreferences.getString(MainApplication.LANGUAGE, "def");  //获取轻量存储的语言设置
        switch (lanTag) {
            case "def":  //没有手动设置，则对应的语言则默认读取手机系统的语言
                String locale = Locale.getDefault().getLanguage();  // 获取手机系统本地语言
                String langFlag = "";
                if (TextUtils.isEmpty(locale)) { //如果为空，默认为中文
                    langFlag = LANG_CN;
                    newLocale = Locale.CHINESE;
                    MainApplication.LANGUAGEID = 1;
                } else if (locale.contains("en")) {
                    langFlag = LANG_EN;
                    newLocale = Locale.ENGLISH;
                    MainApplication.LANGUAGEID = 0;
                } else if (locale.startsWith("zh")) {
                    String region = Locale.getDefault().getDisplayCountry();
                    if(region.equals("香港特別行政區")||region.equals("台灣")){
                        langFlag = LANG_HK;
                        newLocale = Locale.TRADITIONAL_CHINESE;
                        MainApplication.LANGUAGEID = 2;
                    }else{
                        langFlag = LANG_CN;
                        newLocale = Locale.SIMPLIFIED_CHINESE;
                        MainApplication.LANGUAGEID = 1;
                    }
                }
                else {
                    newLocale = Locale.CHINESE;
                }
                sharedPreferences.edit().putString(MainApplication.LANGUAGE, langFlag).apply();  //写入设置语言
                break;
            case LANG_EN: //设置为英语
                newLocale = Locale.ENGLISH;
                MainApplication.LANGUAGEID = 0;
                break;
            case LANG_HK: //设置为繁體
                newLocale = Locale.TRADITIONAL_CHINESE;
                MainApplication.LANGUAGEID = 2;
                break;
            case LANG_CN: //设置为简体
                newLocale = Locale.SIMPLIFIED_CHINESE;
                MainApplication.LANGUAGEID = 1;
                break;
            default://默认为汉语
                newLocale = Locale.SIMPLIFIED_CHINESE;
                MainApplication.LANGUAGEID = 1;
                break;
        }
        context = getLanContext(context, newLocale);  //初始化语言后；获取资源适配后的
        return new ContextWrapper(context);
    }

    /**
     * 初始化Context
     */
    private static Context getLanContext(Context context, Locale pNewLocale) {
        Resources res = context.getApplicationContext().getResources();  //获取 资源 Resources
        Configuration configuration = res.getConfiguration();  //获取 Configuration 资源配置，用于定义配置类,可替换xml配置文件
        // 设置Locale 并初始化 Context
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //API 24    android 7.0 Nougat    牛轧糖
            configuration.setLocale(pNewLocale);
            LocaleList localeList = new LocaleList(pNewLocale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            context = context.createConfigurationContext(configuration);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {  //API 20    android 4.4W KitKat    奇巧巧克力棒
            configuration.setLocale(pNewLocale);
            context = context.createConfigurationContext(configuration);
        }
        return context;
    }


    /**
     * 获取手机设置的语言国家. CN=简体；TW/HK=繁体； 空/US=英语
     */
    public static String getCountry(Context context) {
        String country;
        Resources resources = context.getApplicationContext().getResources();
        //在7.0以上和 7.0一下获取国家的方式有点不一样
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            country = resources.getConfiguration().getLocales().get(0).getCountry();
        } else {
            country = resources.getConfiguration().locale.getCountry();
        }
        return country;
    }

    /***
     * 获取手机设置
     */

}
