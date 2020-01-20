package com.anysou.as_receiptnotice;

import java.util.HashMap;
import java.util.Map;

/**
 * POST 数据加密过滤
 * */

public class PostMapFilter {
    private Map<String, String> unmodifiedmap;  //Map 数据
    private PreferenceUtil preference;          //存储配置设置
    private String posturl;                     //POST地址

    public PostMapFilter(PreferenceUtil preference, Map<String, String> unmodifiedmap, String posturl) {
        this.preference = preference;
        this.unmodifiedmap = unmodifiedmap;
        this.posturl = posturl;
    }

    // 获取最终的设备ID
    public String getDeviceid() {
        String deviceid = preference.getDeviceid();
        if (deviceid.equals(""))  // 设置的设备ID为空，用默认的设备ID(UUID)
            deviceid = DeviceInfoUtil.getUniquePsuedoID();
        else if (preference.isAppendDeviceiduuid())  //如果，在设置的标识设备ID后面，再加上唯一的UUID
            deviceid = deviceid + '-' + DeviceInfoUtil.getUniquePsuedoID();
        else
            deviceid = deviceid;
        return deviceid;
    }

    // 数据加密处理
    public Map getPostMap() {
        Map<String, String> postmap = new HashMap<String, String>();
        postmap.putAll(getLogMap());
        //sign: md5(type+money)
        postmap.put("sign",new MD5().getSignMd5(postmap.get("type"),postmap.get("money")));
        if (preference.isEncrypt()) {  //是否加密
            String encrypt_type = preference.getEncryptMethod(); //加密方法
            if (encrypt_type != null) {
                String key = preference.getPasswd();  //密钥
                EncryptFactory encryptfactory = new EncryptFactory(key);  //根据加密方法选择处理加密
                LogUtil.debugLog("加密方法" + encrypt_type);
                LogUtil.debugLog("加密秘钥" + key);

                Encrypter encrypter = encryptfactory.getEncrypter(encrypt_type);
                if (encrypter != null && key != null) {
                    postmap = encrypter.transferMapValue(postmap);
                    postmap.put("url", this.posturl);
                    if (preference.isSkipEncryptDeviceid())
                        postmap.put("deviceid", getDeviceid());
                }
            }
        } else
            postmap.put("encrypt", "0");
        return postmap;
    }

    // 获取所有MAP信息
    public Map getLogMap() {
        Map<String, String> recordmap = new HashMap<String, String>();
        recordmap.putAll(this.unmodifiedmap);     //put 所有数据
        recordmap.put("url", this.posturl);       //put post地址
        recordmap.put("deviceid", getDeviceid()); //put 设备ID
        //有要推送的自定义选项（用冒号区分键和值，多个使用分号）
        if (preference.getCustomOption().equals("") == false) {
            Map custompostoption = ExternalInfoUtil.getCustomPostOption(preference.getCustomOption());
            if (custompostoption != null)
                recordmap.putAll(custompostoption);
        }
        return recordmap;
    }


}
