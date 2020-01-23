package com.anysou.as_receiptnotice;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

/**
 * Illustrate Decrypt解密说明
 * **/

public class IllustrateDecryptActivity extends AppCompatActivity {

    private TextView text_method;       //解密方法
    private TextView text_passwd;       //解密公式
    private TextView text_iv;           //解密密钥
    private PreferenceUtil preference;  //获取设置存储

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illustratedecrypt);

        initView();   //初始化
        setText();    //设置显示内容
    }


    private void initView() {
        text_method = (TextView) findViewById(R.id.info_text_method);
        text_passwd = (TextView) findViewById(R.id.info_text_passwd);
        text_iv = (TextView) findViewById(R.id.info_text_iv);
        preference = new PreferenceUtil(getBaseContext()); //getBaseContext()： 返回由构造函数指定或setBaseContext()设置的上下文.
    }

    private void setText(){
        String encrypt_type = preference.getEncryptMethod();
        if(encrypt_type==null){
            text_method.setText("您没有设置加密方法");
            return;
        }
        if(encrypt_type.equals("des")){
            String method="DES/CBC/PKCS5Padding";
            text_method.setText("解密方法为:"+method);
            String key = preference.getPasswd();
            if(key!=null){
                text_passwd.setText("解密秘钥为:\n"+key+"(des秘钥必须为8位,如果你设置的不是8位，请修改)");
                if(key.length()!=8) text_iv.setText("当前解密密钥长度不为8，请修改！");
                else text_iv.setText("解密的初始化向量为:"+key);
            }
        }
        if(encrypt_type.equals("md5")){
            text_method.setText("解密方法为:"+"md5校验");
            String key=preference.getPasswd();
            if(key!=null){
                text_passwd.setText("md5加密方法为：\n md5( md5(price + type) + secretkey)");
                text_iv.setText("secretkey为:"+key);
            }
        }

    }
}
