package com.anysou.as_receiptnotice;


import java.util.Map;

/**
 * 加密机
 * **/
public abstract class Encrypter implements IDataTrans{
    protected String key;
    public Encrypter(String key){
    this.key=key;
    }
    public Encrypter(){}
    public abstract Map<String, String> transferMapValue(Map<String, String> params);
}
