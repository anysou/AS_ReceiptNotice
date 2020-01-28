package com.anysou.as_receiptnotice;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 银行识别器
 * */

public class BankDistinguisher {

    public BankDistinguisher(){}

    // 根据号码识别
    public String distinguishByNum(String num){
        Map<String, String> map = ExternalInfoUtil.getBanksMessageNum();
        String whatsback=map.get(num);
        if(whatsback!=null)
            return whatsback;
        else
            return "";

    }

    // 根据信息内容识别银行名称
    public String distinguishByMessageContent(String content){
        LogUtil.debugLog(content);
        // 信息内有“银行” 、“收入|存入|转入|入账|来帐”、银行名称
        if(content.contains("银行") && ExternalInfoUtil.containsBankmessageFeature(content))
        {
            LogUtil.debugLog("包含银行，有入账信息，进一步确定银行名称");
            Stack<String> alternativebank = new Stack<String>();
            Map <String,String> map = ExternalInfoUtil.getAllBanksNameMap();  //银行名 集合
            for (String key : map.keySet()) {
                if(content.contains(key))
                    alternativebank.push(key);
            }
            if(alternativebank.isEmpty())
                return "";
            else
                return map.get(alternativebank.peek());  //peek()返回栈顶元素,但不在堆栈中删除它
        }
        else
            return null;
    }

    // 获取资金
    public  String extractMoney(String content){
        Pattern pattern = Pattern.compile("(收入|存入|转入|入账|来帐)?(\\d{1,3}(,\\d{2,3})*(\\.\\d{0,2})?)元?");
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            String tmp = matcher.group();
            Pattern patternnum = Pattern.compile("((\\d{1,3}(,\\d{2,3})*(\\.\\d{0,2})?))");
            Matcher matchernum = patternnum.matcher(tmp);
            if(matchernum.find())
                return matchernum.group();
            return null;
        }else
            return null;
    }

    // 获取卡号
    public String extractCardNum(String content){
        String pattern = "(尾号\\d{4}的?(卡|账号|账户))";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(content);
        if(m.find())
            return m.group();
        else
            return "";
    }

    // 获取到账时间
    public String extractTime(String content,String time){

        Pattern pattern = Pattern.compile("([0-9]|0[0-9]|1[0-9]|2[0-3]):([0-5][0-9])");
        Matcher matcher = pattern.matcher(content);
        if(matcher.find()){
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                Date date = df.parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY,Integer.parseInt(matcher.group(1)));
                calendar.set(Calendar.MINUTE,Integer.parseInt(matcher.group(2)));
                return df.format(calendar.getTime());
            } catch (ParseException e) {
                return time;
            }

        }else
            return time;
    }


}
