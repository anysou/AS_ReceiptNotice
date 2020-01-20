package com.anysou.as_receiptnotice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.crypto.Cipher;

/**
 * DES 加密方式
 * **/
public class DES extends Encrypter {
    public DES(String key) {
        super(key);
    }

    public Map<String, String> transferMapValue(Map<String, String> params) {
        Map<String, String> postmap = new HashMap<String, String>();
        Iterator entries = params.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String paramkey = (String) entry.getKey();
            String paramvalue = (String) entry.getValue();
            String desStr = DESUtilWithIV.des(paramvalue, key, Cipher.ENCRYPT_MODE);
            postmap.put(paramkey, desStr);
        }
        postmap.put("encrypt", "1");
        LogUtil.debugLogWithJava("调试，开始加密字符串");
        LogUtil.debugLogWithJava("加密后的map");
        LogUtil.debugLogWithJava(postmap.toString());
        return postmap;
    }

}