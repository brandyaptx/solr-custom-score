package com.easy.custom.tools;

import org.ahocorasick.trie.Trie;

import java.io.*;
import java.util.LinkedList;


/**
 * Created by lwp on 2017/3/14.
 */
public class Trietest {
    public static void main(String[] args) throws IOException {
        BufferedReader br;
        br = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\solr-custom-score-master\\resource\\dictionary.txt")));
        String line;


        LinkedList<String> dictionary = new LinkedList<String>();
        while ((line = br.readLine()) != null)
        {
            dictionary.add(line);
        }
        br.close();
        long start = System.currentTimeMillis();
        Trie unicodeTrie  = Trie.builder().addKeywords(dictionary).build();
        System.out.printf("unicodeTrie adding time:%dms%n", System.currentTimeMillis() - start);



        int pressure = 100000;
        String text = "http://share.iclient.ifeng.com/news/sharenews.f?aid=82969287/";

        start = System.currentTimeMillis();
        System.out.println(unicodeTrie.parseText(text));
        System.out.printf("asciiTrie building time:%dms%n", System.currentTimeMillis() - start);


        start = System.currentTimeMillis();
        for (int i = 0; i < pressure; ++i)
        {
            unicodeTrie.parseText(text);
        }
        System.out.printf("unicodeTrie used time:%dms%n", System.currentTimeMillis() - start);
    }

}
