package com.gram.gram_landlord.protocols.request;

public class MultipleWagerRequest implements Request {
    private int agreed;

    public MultipleWagerRequest() {
    }

    public MultipleWagerRequest(int agreed) {
        this.agreed = agreed;
    }

    public int getAgreed() {
        return agreed;
    }

    public void setAgreed(int agreed) {
        this.agreed = agreed;
    }
}
