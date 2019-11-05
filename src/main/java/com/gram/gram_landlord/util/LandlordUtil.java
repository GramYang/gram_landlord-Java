package com.gram.gram_landlord.util;

import com.gram.gram_landlord.Constant;
import com.gram.gram_landlord.entity.Player;

import java.util.*;

public class LandlordUtil {

    /**
     * 根据牌桌号获取座位号
     */
    public static int getSeatNum(int tableNum, int tablePlayerCount) {
        return (tableNum - 1) * 3 + tablePlayerCount + 1;
    }

    /**
     * 获取随机牌，牌洗了三遍
     */
    public static ArrayList<Integer> getRandomCards() {
        ArrayList<Integer> source=new ArrayList<>(Arrays.asList(Constant.CARDS));
        Collections.shuffle(source);
        Collections.shuffle(source);
        Collections.shuffle(source);
        return source;
    }

    public static int getRandomLandlord(int tableNum) {
        Random random = new Random();
        return (tableNum - 1) * 3 + 1 + (int)(random.nextFloat() * 2.0f);
    }

    public static int getLeftRivalSeatNum(int yourSeatNum, List<Player> players) {
        List<Integer> list = new ArrayList<>();
        for(Player player : players) list.add(player.getSeatNum());
        Integer max = Collections.max(list);
        if(max - yourSeatNum == 0) return max - 1;
        else if(max - yourSeatNum == 1) return max - 2;
        else return max;
    }

    public static int getRightRivalSeatNum(int yourSeatNum, List<Player> players) {
        List<Integer> list = new ArrayList<>();
        for(Player player : players) list.add(player.getSeatNum());
        Integer max = Collections.max(list);
        if(max - yourSeatNum == 0) return max - 2;
        else if(max - yourSeatNum == 1) return max;
        else return max - 1;
    }
}
