package com.kinstalk.her.cmccmode.utils;

import java.util.regex.Pattern;

public class RegexItem {
    public Pattern regex;
    public String[] tags;
    public String intentName;

    public RegexItem(Pattern regex, String[] tags){
        this.regex = regex;
        this.tags = tags;
    }

    public RegexItem(Pattern regex, String[] tags, String intentName){
        this.regex = regex;
        this.tags = tags;
        this.intentName = intentName;
    }
}
