/*
 * Created By WeihuaGu (email:weihuagu_work@163.com)
 * Copyright (c) 2017
 * All right reserved.
 */

package com.anysou.as_receiptnotice;

import com.anysou.aslogger.ASLogApplication;
import com.anysou.aslogger.ASLogFileUtils;
import com.anysou.aslogger.ASLogIConfig;

import java.io.File;
import java.util.ArrayList;

/**
 * 文件日志LIST管理部分，继承 ASLogFileUtils
 */

public class FileLogListUtil extends ASLogFileUtils{

    // 清除日志文件（删除文件）
    public static boolean clearLogFile() {
        try {
            File file = new File(ASLogApplication.getAPP().getFilesDir(), ASLogIConfig.fileName);
            if(file.delete())
                return true;
            else
                return false;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 将读取日志文件转为 LIST
    public static ArrayList getLogList(){
        File file = new File(ASLogApplication.getAPP().getFilesDir(), ASLogIConfig.fileName);
        if (!file.exists())
            return null;

        OneFileListUtil fileutil = new OneFileListUtil(file);
        ArrayList filelist = fileutil.getFileList();
        String startflag="*********************************";
        String endflag="------------------------------------------";
        ArrayList filemergelist=fileutil.mergeByFlagline(startflag,endflag,filelist);
        return filemergelist;
    }



}
