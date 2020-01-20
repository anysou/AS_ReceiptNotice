package com.anysou.as_receiptnotice;

import java.util.Map;

/**
 * 异步响应 接口，处理POST的返回结果
 * **/
public interface AsyncResponse {
    // post 成功
    public void onDataReceivedSuccess(String[] returnstr);
    // post 失败
    public void onDataReceivedFailed(String[] returnstr, Map<String ,String> postedmap);
}