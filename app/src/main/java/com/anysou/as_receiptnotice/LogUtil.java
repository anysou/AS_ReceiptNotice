
package com.anysou.as_receiptnotice;

import android.util.Log;

import com.anysou.aslogger.ASLogger;
import com.jeremyliao.liveeventbus.LiveEventBus;


/**
 * 日志 分类显示
 * */

public class LogUtil {

        public static String TAG="NLService";
        public static String DEBUGTAG="NLDebugService";
        public static String TIMETAG="TIME";

        public static void TimeDLog(String info) {Log.d(TIMETAG,info); }

        public static void infoLog(String info){ Log.i(TAG,info); }

        public static void debugLog(String info){Log.d(TAG,info); }
        
        public static void debugLogWithDeveloper(String info){
                Log.d(DEBUGTAG,info);
        }

        public static void debugLogWithJava(String info){
                System.out.println(DEBUGTAG+":"+info);
        }

        // 推送记录
        public static void postRecordLog(String tasknum, String post){
                ASLogger.i("*********************************");
                ASLogger.i("开始推送", "随机序列号:"+tasknum);
                ASLogger.i(post);
        }

        // 推送返回记录
        public static void postResultLog(String tasknum, String result, String returnstr){

                ASLogger.i("推送结果","随机序列号:"+tasknum);
                ASLogger.i("推送结果",result);
                ASLogger.i("返回内容",returnstr);
                ASLogger.i("------------------------------------------");

                //发送一条即时消息
                LiveEventBus
                        .get("update_recordlist")
                        .post("update");
        }

}
