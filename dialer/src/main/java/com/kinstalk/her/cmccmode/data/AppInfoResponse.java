package com.kinstalk.her.cmccmode.data;

public class AppInfoResponse {
    private int recode;
    private String desc;
    private AppInfo result;

    public AppInfoResponse() {
    }

    public AppInfoResponse(int recode, String desc, AppInfo result) {
        this.recode = recode;
        this.desc = desc;
        this.result = result;
    }

    public int getRecode() {
        return recode;
    }

    public void setRecode(int recode) {
        this.recode = recode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public AppInfo getResult() {
        return result;
    }

    public void setResult(AppInfo result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "AppInfoResponse{" +
                "recode=" + recode +
                ", desc='" + desc + '\'' +
                ", result=" + result +
                '}';
    }
}
