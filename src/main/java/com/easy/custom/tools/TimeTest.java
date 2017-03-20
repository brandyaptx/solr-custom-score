package com.easy.custom.tools;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

/**
 * Created by lwp on 2017/3/13.
 */
public class TimeTest {
    public static  void  main(String arg[]){
        DateTime dt1=new DateTime(2017,1,12,8,18,20,0);
        DateTime dt2=new DateTime();
        System.out.println("天数差:"+ Days.daysBetween(dt1, dt2).getDays());
        System.out.println("小时差:"+ Hours.hoursBetween(dt1, dt2).getHours());
        System.out.println("分钟差:"+ Minutes.minutesBetween(dt1, dt2).getMinutes());
        System.out.println("打分:"+ Math.pow((1.0f/(float)(Hours.hoursBetween(dt1, dt2).getHours()+1)),0.1));
    }
}
