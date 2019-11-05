package com.gram.gram_landlord.protocols.request;

public class EnterTableRequest implements Request {
    private String userName;
    private int tableNum;

    public EnterTableRequest() {
    }

    public EnterTableRequest(String userName, int tableNum) {
        this.userName = userName;
        this.tableNum = tableNum;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getTableNum() {
        return tableNum;
    }

    public void setTableNum(int tableNum) {
        this.tableNum = tableNum;
    }
}
