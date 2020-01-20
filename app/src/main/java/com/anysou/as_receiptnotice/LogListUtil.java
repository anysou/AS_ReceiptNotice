package com.anysou.as_receiptnotice;

import android.util.Log;

import com.anysou.aslogger.ASLogger;
import com.jeremyliao.liveeventbus.LiveEventBus;

/**
 * 记录日志单元
 */

public class LogListUtil {

        public static String TAG="NLService";
        public static String DEBUGTAG="NLDebugService";
        public static String EXCEPTIONTAG="NLExceptionService";
        public static void infoLog(String info){
                Log.i(TAG,info);
        }

        public static void debugLog(String info){
                Log.d(TAG,info);
        }

        public static void debugLogWithDeveloper(String info){
                Log.d(DEBUGTAG,info);
        }

        public static void debugLogWithJava(String info){
                System.out.println(DEBUGTAG+":"+info);
        }

        public static void postRecordLog(String tasknum, String post){
                ASLogger.i("*********************************");
                ASLogger.i("开始推送", "随机序列号:"+tasknum);
                ASLogger.i(post);
        }


        public static void postResultLog(String tasknum, String result, String returnstr){

                ASLogger.i("推送结果","随机序列号:"+tasknum);
                ASLogger.i("推送结果",result);
                ASLogger.i("返回内容",returnstr);
                ASLogger.i("------------------------------------------");

                LiveEventBus
                        .get("update_recordlist")
                        .post("update");
        }

}
