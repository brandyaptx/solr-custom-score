package com.easy.custom.tools;

import org.joda.time.DateTime;
import org.joda.time.Hours;



public class ScoreTools {


    /***
     *
     * @param time 传入当前计算的日期毫秒数
     * @return 该日期的一个动态加权评分
     */
    public static float getTimeScore(long time){
        if(time==0){//没有日期的数据，不做加权操作
            return 1;
        }
        int year = (int) (time / 1000000);
        int tmp = (int) (time % 1000000);
        int month = tmp /10000;
        tmp = tmp % 10000;
        int day = tmp / 100;
        int hour = (int) (time % 100);
        DateTime now=new DateTime();
        DateTime varTime=new DateTime(year,month,day,hour,0,0);
        int between= Hours.hoursBetween(varTime,now).getHours();
        float score= (float) Math.pow((1.0f/(float)(between+1)),0.1);
//        return score*1.5f;

        return score;

    }


//    /***
//     *
//     * @param money 传入需要计算的注册资金
//     * @param maxTimes 设置加权因子的最大倍数上限
//     * @param money_base 注册资金基数
//     * @return 该资金的一个动态评分值
//     */
//    public static float getMoneyScore(Double money,int maxTimes,int money_base){
//        if(money==0){//没有注册资金的数据，不做加权操作
//            return 1;
//        }
//        int mtimes=(int)(money/money_base);
//        float score=1;
//        if(mtimes>0&&mtimes<=maxTimes){
//            return mtimes*score;
//        }else if(mtimes>maxTimes){//超过上限者，统一按上限值乘以1.5倍算
//            return score*maxTimes*1.5f;
//        }
//        return score;
//
//    }



}

