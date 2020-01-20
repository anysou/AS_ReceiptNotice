package com.anysou.as_receiptnotice;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *  POST 任务类 继承 异步任务 AsyncTask
 *
 *  AsyncTask：对线程间的通讯做了包装，是后台线程和UI线程可以简易通讯：后台线程执行异步任务，将result告知UI线程。
 * 继承AsyncTask<Params,Progress,Result>
 *     Params:输入参数。对应的是调用自定义的AsyncTask的类中调用 excute()方法中传递的参数。如果不需要传递参数，则直接设为Void即可。
 *     Progress：子线程执行的百分比。如果不需要，则直接设为Void即可。
 *     Result：返回值类型。和doInBackground（）方法的返回值类型保持一致。
 * **/


public class PostTask extends AsyncTask<Map<String, String>, Void, String[]> {

    public String TAG="NLService";
    public AsyncResponse asyncResponse;   // 异步响应 接口，处理POST的返回结果

    public String randomtasknum;          // 随机数
    public void setRandomTaskNum(String num){
        this.randomtasknum = num;
    }
    public Map<String ,String> recordpostmap;
    private NetUtil netutil=new NetUtil();     //网络post 实现

    //public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");  // POST 提交数据的类型
    //OkHttpClient client = new OkHttpClient();

    // 设置异步返回
    public void setOnAsyncResponse(AsyncResponse asyncResponse)
    {
        this.asyncResponse = asyncResponse;
    }

    /*
     * 第一个执行的方法
     * 执行时机：在执行实际的后台操作前，被UI 线程调用。
     * 作用：可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /*
     * 执行时机：在onPreExecute 方法执行后马上执行，该方法运行在后台线程中。
     * 作用：主要负责执行那些很耗时的后台处理工作。可以调用 publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override  //返回值类型和Result保持一致。参数：若无就传递Void；若有，就可用Params
    protected String[] doInBackground(Map<String,String> ... key) {
        recordpostmap = key[0];
        Map<String ,String> postmap = new HashMap<String,String>();
        postmap.putAll(key[0]);
        if(postmap==null)
            return null;
        String url = postmap.get("url");
        if(url==null)
            return null;

        String[] resultstr = new String[3];  //返回字符串数组。共3条。0=随机数 1=真或否 2=返回数据
        resultstr[0] = this.randomtasknum;
        postmap.remove("url");
        String protocol = UrlUtil.httpOrHttps(url);
        String postjson = map2Json(postmap);
        if("http".equals(protocol)){
            try{
                Log.d(TAG,"http post task  url:"+url);
                Log.d(TAG,"http post task postjson:"+postjson);
                String returnstr=netutil.httppost(url,postjson);
                resultstr[1]="true";
                resultstr[2]=returnstr;
                return resultstr;
            }catch(IOException e){}
        }
        if("https".equals(protocol)){
            try{
                Log.d(TAG,"https post task  url:"+url);
                Log.d(TAG,"https post task postjson:"+postjson);
                String returnstr=netutil.httpspost(url,postjson);
                resultstr[1]="true";
                resultstr[2]=returnstr;
                return resultstr;
            }catch(IOException e){}
        }
        return null;
    }
    //map 格式转为 json格式
    public String map2Json(Map<String,String> map){
        String mapjson="";
        Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            mapjson=mapjson+'"'+entry.getKey()+'"' + ":"+'"'+entry.getValue()+'"'+",";
        }
        int strlength=(int)mapjson.length();
        mapjson=mapjson.substring(0,(strlength-1));
        mapjson="{"+mapjson+"}";
        return mapjson;
    }


    /*
     * 执行时机：在doInBackground 执行完成后，将被UI 线程调用
     * 作用：后台的计算结果将通过该方法传递到UI 线程，并且在界面上展示给用户
     * result:上面doInBackground执行后的返回值，所以这里是"执行完毕"
     */
    @Override //无返回值类型。参数：和Result保持一致
    protected void onPostExecute(String[] resultstr) {
        super.onPostExecute(resultstr);
        if (resultstr != null)
        {
            asyncResponse.onDataReceivedSuccess(resultstr); //将结果传给回调接口中的函数
        }
        else {
            String [] errstr = new String[3];
            errstr[0]=this.randomtasknum;
            errstr[1]="false";
            errstr[2]="";
            if(recordpostmap.get("repeatnum")!=null){
                String repeatnumstr=recordpostmap.get("repeatnum");
                int num=Integer.parseInt(repeatnumstr)+1;
                recordpostmap.put("repeatnum",String.valueOf(num));
                //key 存在
            }
            else
                recordpostmap.put("repeatnum","1");
            asyncResponse.onDataReceivedFailed(errstr,recordpostmap);
        }

    }

}
