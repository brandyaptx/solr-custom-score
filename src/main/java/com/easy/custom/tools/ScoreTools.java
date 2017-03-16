package com.easy.custom.tools;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.ahocorasick.trie.Trie;

import java.io.*;
import java.util.LinkedList;


public class ScoreTools {

    private static Trie unicodeTrie;
    static {
        //加载重点网站站点列表，使用ac自动机进行多模式匹配
        BufferedReader br = new BufferedReader(new InputStreamReader(ScoreTools.class.getClassLoader().getResourceAsStream("/dictionary.txt")));
        String line;
        LinkedList<String> dictionary = new LinkedList<String>();
        try {
            while (null != (line = br.readLine())) {
                dictionary.add(line);
            }
            unicodeTrie = Trie.builder().addKeywords(dictionary).build();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static float getTimeScore(long time) {
        if (time == 0) {//没有日期的数据，不做加权操作
            return 0.8f;
        }
        int year = (int) (time / 1000000);
        int tmp = (int) (time % 1000000);
        int month = tmp / 10000;
        tmp = tmp % 10000;
        int day = tmp / 100;
        int hour = (int) (time % 100);
        DateTime now = new DateTime();
        DateTime varTime = new DateTime(year, month, day, hour, 0, 0);
        int between = Hours.hoursBetween(varTime, now).getHours();
        float score = (float) Math.pow((1.0f / (float) (between + 1)), 0.1);
//        return score*1.5f;
        return score;
    }

    public static float getSiteScore(String site_url) {
        if(site_url == null) {//没有url的数据，不做加权操作
            return 0.8f;
        }
        if(unicodeTrie.parseText(site_url).isEmpty())
            return 0.8f;
        else
            return 1.0f;
    }
}

