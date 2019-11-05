package com.gram.gram_landlord.protocols.request;

public class CancelReadyRequest implements Request {
    private boolean isCancelReady;

    public CancelReadyRequest(boolean isCancelReady) {
        this.isCancelReady = isCancelReady;
    }

    public boolean isCancelReady() {
        return isCancelReady;
    }
}
