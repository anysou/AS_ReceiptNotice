package com.anysou.as_receiptnotice;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

/**
 * 主页面的滑动适配器
 * 继承 FragmentStateAdapter（滑过后会保存当前界面，以及下一个界面和上一个界面（如果有），最多保存3个，其他会被销毁掉）
 * **/

public class FragmentsAdapterHome extends FragmentStateAdapter {

    //存放分片页面
    private ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

    public FragmentsAdapterHome(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        //添加分片的页面
        fragmentArrayList.add(new FragmentHello());    //HELLO 首页
        fragmentArrayList.add(new FragmentLogList());  //日志显示页
    }

    @NonNull
    @Override  //获取当前片的序号
    public Fragment createFragment(int position) {
        return fragmentArrayList.get(position);
    }

    @Override  //获取总分片数
    public int getItemCount() {
        return fragmentArrayList.size();
    }
}
