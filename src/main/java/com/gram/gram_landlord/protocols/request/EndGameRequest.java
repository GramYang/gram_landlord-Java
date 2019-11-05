package com.gram.gram_landlord.protocols.request;

public class EndGameRequest implements Request {
    private int winnerSeatNum;

    public EndGameRequest() {
    }

    public EndGameRequest(int winnerSeatNum) {
        this.winnerSeatNum = winnerSeatNum;
    }

    public int getWinnerSeatNum() {
        return winnerSeatNum;
    }

    public void setWinnerSeatNum(int winnerSeatNum) {
        this.winnerSeatNum = winnerSeatNum;
    }
}
