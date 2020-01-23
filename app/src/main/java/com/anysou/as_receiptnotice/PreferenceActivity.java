package com.anysou.as_receiptnotice;

import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 喜好设置界面
 *
 * Android实现个人中心设置界面  https://www.jianshu.com/p/89840f06d53f    https://blog.csdn.net/djp13276475747/article/details/87738097
 * 
 * Preference 实现设置界面 https://www.jianshu.com/p/2b76fda697a4
 * */

public class PreferenceActivity extends AppCompatActivity {


    @Override  //@Nullable 可以传入NULL值（空指针） @Nonnull 不可以为空指针
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getFragmentManager()是所在fragment 父容器的fragment管理 ； begintransaction();开启一个新的事务
        //replace 替换一个已被添加进视图容器的Fragment。 android.R.id.content 是整个应用程序屏幕的容器
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    // 继承 PreferenceFragment 喜好设置分片
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }
    }



}
