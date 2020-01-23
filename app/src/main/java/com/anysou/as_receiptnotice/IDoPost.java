package com.anysou.as_receiptnotice;

import java.util.Map;

/**
 * 接口 要POST的数据  通过此接口；进入 NLService.java -> public void doPost(Map<String, String> params)
 * */
public interface IDoPost {
        public  void doPost(Map<String, String> params);
}
