package com.gram.gram_landlord.protocols.response;

public class ReadyResponse implements Response {
    private boolean ready;

    public ReadyResponse() {
    }

    public ReadyResponse(boolean isReady) {
        this.ready = isReady;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
