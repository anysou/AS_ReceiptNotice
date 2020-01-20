package com.anysou.as_receiptnotice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

/**
 * HELLO 首页； 使用 fragment_hello 布局
 * */

public class FragmentHello extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflater.inflate 从指定的xml资源中扩展新的视图层次结构。
        return inflater.inflate(R.layout.fragment_hello,container, false);
    }
}
