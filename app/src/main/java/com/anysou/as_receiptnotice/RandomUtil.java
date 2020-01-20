
package com.anysou.as_receiptnotice;
import java.util.Random;

/**
 * 获取随机数
 * */
public class RandomUtil {
    public static String getRandomTaskNum(){
            Random rand = new Random();
            return String.valueOf(rand.nextInt(9000) + 1000);
    }
}
