package com.kinstalk.her.cmccmode.utils;

public class CmdStructure {
    private String intentName;
    private String key;
    private String value;

    public CmdStructure(String intentName, String key, String value) {
        this.intentName = intentName;
        this.key = key;
        this.value = value;
    }

    public String getIntentName() {
        return intentName;
    }

    public void setIntentName(String intentName) {
        this.intentName = intentName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "CmdStructure{" +
                "intentName='" + intentName + '\'' +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
