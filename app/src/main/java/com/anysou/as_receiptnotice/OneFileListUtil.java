/*
 * Created By WeihuaGu (email:weihuagu_work@163.com)
 * Copyright (c) 2017
 * All right reserved.
 */

package com.anysou.as_receiptnotice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * 文件处理单元： 读文件按行转为动态数组； 将动态数组内容按指定开始和结束转为字符串添加到新的动态数组里
 * **/

public class OneFileListUtil{

        private File file;
        public OneFileListUtil(File file){
                this.file=file;
        }

        //  读取文件，按行 转换为 动态数组
        public  ArrayList getFileList(){
                ArrayList<String> arrayList = new ArrayList<String>();
                FileReader fr = null;
                try{
                        if (!file.exists())
                                return null;

                        fr = new FileReader(file);
                        BufferedReader bf = new BufferedReader(fr);
                        String str;
                        while ((str = bf.readLine()) != null) {
                                arrayList.add(str);  //按行处理
                        }
                        if(arrayList.size()==0)
                                return null;
                        else
                                return arrayList;
                } catch (Throwable ex) {
                        ex.printStackTrace();
                } finally {
                        try {
                                if (fr != null)
                                        fr.close();  //确保关闭文件
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
                return null;
        }



        // 将动态数组的内容按 指定的开头和结尾列表转为字符串 再组合成新的动态数组
        public  ArrayList mergeByFlagline(String startflagline,String endflagline,ArrayList filelist){
                if(filelist.size()==0)
                        return null;
                ArrayList<String> merge = new ArrayList<String>();  //用于返回的动态数组
                Iterator fileiterator = filelist.iterator();   //Iterator(迭代器)是一个接口,它的作用就是遍历容器的所有元素
                Deque<String> onegroup = new LinkedList<String>();  //双向链表。它也可以被当作堆栈、队列或双端队列进行操作。
                while (fileiterator.hasNext()) {
                        String o = (String)fileiterator.next();
                        onegroup.offerLast(o);  //添加元素到队尾
                        //peekFirst 取队首元素但不删除    peekLast 取队尾元素但不删除  contains包含
                        if(onegroup.peekFirst().contains(startflagline) && onegroup.peekLast().contains(endflagline)){
                                merge.add(clearOnegroup(onegroup));  //将对列转为字符串；再添加到动态数组
                        }
                }
                return merge;
        }


        // 将队列 转为字符串，用换行符连接
        //deque 队列容器为一个给定类型的元素进行线性处理,像向量一样,它能够快速地随机访问任一个元素,并且能够高效地插入和删除容器的尾部元素。
        private String clearOnegroup(Deque<String> onegroup){
                String tmp="";
                while(onegroup.size()>0){
                        String first = onegroup.pollFirst(); //pollFirst 取队首元素并删除
                        tmp = tmp + "\n" + first;
                }
                return tmp;
        }


}
