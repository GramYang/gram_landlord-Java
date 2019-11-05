package com.gram.gram_landlord;

import java.util.HashMap;

public interface Constant {
    String LINE_SEPARATOR = "\r\n";
    String DES_KEY = "zhangxiaorong is god of gamblers";
    //用key值来表示牌的大小，20的余数即可
    Integer[] CARDS = new Integer[] {1,2,3,4,5,6,7,8,9,10,11,12,13,21,22,23,24,25,26,
            27,28,29,30,31,32,33,41,42,43,44,45,46,47,48,49,50,51,52,53,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75};
    HashMap<String, String> userName2Password = new HashMap<String, String>() {
        {
            put("yangping", "111");put("yangshu", "222");put("zhangxiaorong", "333");
//            put("杨平", "44X4bFlcamP9+0n5PvbPsQ==");put("杨舒", "UUMvtRuaDNY4O3al+a3fnw==");put("张晓蓉", "JboR/+ix11OwpdJ3hoXCM3riU6vhf6Gm");
        }
    };
}
