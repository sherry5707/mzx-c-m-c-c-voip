package com.kinstalk.her.cmccmode.data;

public class AppInfo {
    private String appKey;
    private String appsecret;

    public AppInfo() {
    }

    public AppInfo(String appKey, String appsecret) {
        this.appKey = appKey;
        this.appsecret = appsecret;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getAppsecret() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "appKey='" + appKey + '\'' +
                ", appsecret='" + appsecret + '\'' +
                '}';
    }
}
