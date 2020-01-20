package com.anysou.as_receiptnotice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.ArrayList;
import java.util.Collections;

/**
 * 日志显示 分片页面
 * **/

public class FragmentLogList extends Fragment {
    private RecyclerView recyclerView;   //RecyclerView是Android一个更强大的控件,其不仅可以实现和ListView同样的效果,还有优化了ListView中的各种不足。其可以实现数据纵向滚动,也可以实现横向滚动(ListView做不到横向滚动)。
    private RecyclerView.LayoutManager layoutManager; //RecyclerView的内容线性Layout管理
    private AdapterLogList mAdapter;     //日志显示适配器
    private ArrayList loglist;           //日志LIST



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflater.inflate 从指定的xml资源中扩展新的视图层次结构。
        return inflater.inflate(R.layout.fragment_loglist, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initLoglistView(false);
        subMessage();
    }

    //初始化
    private void initLoglistView(boolean reverseorder) {
        recyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
        layoutManager = new LinearLayoutManager(getContext()); //线性Layout管理
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new AdapterLogList(getContext());
        loglist = FileLogListUtil.getLogList();  //获取日志列表
        if (loglist == null) {
            loglist = new ArrayList<String>();
            loglist.add("推送记录为空");
        }
        //LogUtil.debugLogWithDeveloper("打印通过filelogutil获取到的file log list");
        if(reverseorder)
            Collections.reverse(loglist);
        mAdapter.setLoglist(loglist);
        recyclerView.setAdapter(mAdapter);
    }

    //清空日志
    private void clearLog() {
        FileLogListUtil.clearLogFile();
        loglist.clear();
        mAdapter.notifyDataSetChanged();
        Toast.makeText(MainApplication.getAppContext(), "已经清空日志", Toast.LENGTH_SHORT).show();
    }

    private void showReverse() {
        initLoglistView(true);
    }

    //更新显示
    private void updateList() {
        loglist.clear();
        loglist.addAll(FileLogListUtil.getLogList());
        mAdapter.notifyDataSetChanged();
        LogListUtil.debugLog("更新Loglist in Fragment列表:");
    }

    //发送消息
    private void subMessage() {
        LiveEventBus
                .get("update_recordlist", String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String s) {
                        LogListUtil.debugLog("收到订阅消息:update_recordlist " + s);
                        updateList();
                    }
                });
    }
}
