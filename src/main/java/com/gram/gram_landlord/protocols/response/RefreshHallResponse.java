package com.gram.gram_landlord.protocols.response;


import com.gram.gram_landlord.entity.HallTable;

import java.util.ArrayList;

public class RefreshHallResponse implements Response {
    private ArrayList<HallTable> hallTables;

    public RefreshHallResponse() {
    }

    public RefreshHallResponse(ArrayList<HallTable> hallTables) {
        this.hallTables = hallTables;
    }

    public ArrayList<HallTable> getHallTables() {
        return hallTables;
    }

    public void setHallTables(ArrayList<HallTable> hallTables) {
        this.hallTables = hallTables;
    }
}
