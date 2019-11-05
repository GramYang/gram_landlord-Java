package com.gram.gram_landlord.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Table {
    private List<Player> players = new ArrayList<>(3); //玩家列表
    private Player landlord; //地主
    private ArrayList<Integer> cards = new ArrayList<>(); //洗好的牌
    private int tableNum; //牌桌号
    private int playCount = 0; //目前的玩家数
    //保存牌桌上已经打出去的牌，key是1-15的牌面，value是牌面数
    private HashMap<Integer, Integer> throwOutCards = new HashMap<Integer, Integer>(15) {
        {
            put(1,0);put(2,0);put(3,0);put(4,0);put(5,0);put(6,0);put(7,0);put(8,0);put(9,0);put(10,0);
            put(11,0);put(12,0);put(13,0);put(14,0);put(15,0);
        }
    };
    //记录上一次传递的牌
    private List<Integer> lastCardsOut = new ArrayList<>();
    //座位号：手中持有的牌数
    private HashMap<Integer, Integer> playersCardsCount = new HashMap<>();
    private ArrayList<Integer> threeCards = new ArrayList<>();
    private int readyCount = 0; //准备了的玩家数，满3触发抢地主
    private int passLandlordCount = 0; //传递地主的人数，满3最初的地主强制成为地主
    private int wagerMultipleNum = 1; //加倍倍数
    private int answerMultipleNum = 0; //其他玩家同意加倍数的人数
    private int agreedMultipleResult = 0; //其他玩家同意加倍数的结果
    private int continuousPass = 0; //累计出现pass的次数
    private boolean isWait = true; //等待玩家进入
    private boolean isRob = false; //玩家全部确认，开始抢地主
    private boolean isPlay = false; //抢地主结束，开始游戏

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Player getLandlord() {
        return landlord;
    }

    public void setLandlord(Player landlord) {
        this.landlord = landlord;
    }

    public ArrayList<Integer> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Integer> cards) {
        this.cards = cards;
    }

    public int getTableNum() {
        return tableNum;
    }

    public void setTableNum(int tableNum) {
        this.tableNum = tableNum;
    }

    public synchronized int getPlayCount() {
        return playCount;
    }

    public synchronized void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public HashMap<Integer, Integer> getThrowOutCards() {
        return throwOutCards;
    }

    public void setThrowOutCards(HashMap<Integer, Integer> throwOutCards) {
        this.throwOutCards = throwOutCards;
    }

    public List<Integer> getLastCardsOut() {
        return lastCardsOut;
    }

    public void setLastCardsOut(List<Integer> lastCardsOut) {
        this.lastCardsOut.clear();
        this.lastCardsOut.addAll(lastCardsOut);
    }

    public HashMap<Integer, Integer> getPlayersCardsCount() {
        return playersCardsCount;
    }

    public void setPlayersCardsCount(HashMap<Integer, Integer> playersCardsCount) {
        this.playersCardsCount = playersCardsCount;
    }

    public ArrayList<Integer> getThreeCards() {
        return threeCards;
    }

    public void setThreeCards(ArrayList<Integer> threeCards) {
        this.threeCards.clear();
        this.threeCards.addAll(threeCards);
    }

    public synchronized int getReadyCount() {
        return readyCount;
    }

    public synchronized void setReadyCount(int readyCount) {
        this.readyCount = readyCount;
    }

    public int getPassLandlordCount() {
        return passLandlordCount;
    }

    public void setPassLandlordCount(int passLandlordCount) {
        this.passLandlordCount = passLandlordCount;
    }

    public int getWagerMultipleNum() {
        return wagerMultipleNum;
    }

    public void setWagerMultipleNum(int wagerMultipleNum) {
        this.wagerMultipleNum = wagerMultipleNum;
    }

    public synchronized int getAnswerMultipleNum() {
        return answerMultipleNum;
    }

    public synchronized void setAnswerMultipleNum(int answerMultipleNum) {
        this.answerMultipleNum = answerMultipleNum;
    }

    public synchronized int getAgreedMultipleResult() {
        return agreedMultipleResult;
    }

    public synchronized void setAgreedMultipleResult(int agreedMultipleResult) {
        this.agreedMultipleResult = agreedMultipleResult;
    }

    public synchronized int getContinuousPass() {
        return continuousPass;
    }

    public synchronized void setContinuousPass(int continuousPass) {
        this.continuousPass = continuousPass;
    }

    public boolean isWait() {
        return isWait;
    }

    public void setWait(boolean wait) {
        isWait = wait;
    }

    public boolean isRob() {
        return isRob;
    }

    public void setRob(boolean rob) {
        isRob = rob;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }
}
