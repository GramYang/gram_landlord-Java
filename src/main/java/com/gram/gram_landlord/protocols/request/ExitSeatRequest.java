package com.gram.gram_landlord.protocols.request;

public class ExitSeatRequest implements Request {
    private int yourSeatNum;

    public ExitSeatRequest() {
    }

    public ExitSeatRequest(int yourSeatNum) {
        this.yourSeatNum = yourSeatNum;
    }

    public int getYourSeatNum() {
        return yourSeatNum;
    }

    public void setYourSeatNum(int yourSeatNum) {
        this.yourSeatNum = yourSeatNum;
    }
}
