package com.kinstalk.her.cmccmode.utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class RegexList {
    public ArrayList<RegexItem> contents;

    {
        contents = new ArrayList<>();
        contents.add(new RegexItem(Pattern.compile("打电话给+(.{1,})"),new String[]{"contact"},"makeCall"));
        contents.add(new RegexItem(Pattern.compile("给(?:(.{1,}))+打电话"),new String[]{"contact"},"makeCall"));
        //contents.add(new RegexItem(Pattern.compile(""), new String[]{}));
    }
}
