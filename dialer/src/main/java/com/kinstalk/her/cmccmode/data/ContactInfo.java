package com.kinstalk.her.cmccmode.data;

import android.util.Log;

import com.github.promeg.pinyinhelper.Pinyin;
import com.kinstalk.her.cmccmode.utils.NumberToChineseUtil;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;

import static com.kinstalk.her.cmccmode.utils.PinYinTool.isPinYinChar;


public class ContactInfo {
    private int id;
    private int contactId;
    private String nickname;
    /**
     * 排序字母
     */
    private String sortKey = "";

    public ContactInfo() {
    }

    /**
     *
     * @param contactId
     * @param nickname
     */
    public ContactInfo(int contactId, String nickname) {
        this.contactId = contactId;
        this.nickname = nickname;
    }

    public ContactInfo(int id, int contactId, String nickname) {
        this.id = id;
        this.contactId = contactId;
        this.nickname = nickname;
    }

    public void setInfo(int contactId, String nickname, String sortKey) {
        this.id = id;
        this.contactId = contactId;
        this.nickname = nickname;
        this.sortKey = sortKey;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContactId() {
        return String.valueOf(contactId);
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        for (char c : nickname.toCharArray()) {
            this.sortKey = sortKey + Pinyin.toPinyin(isPinYinChar(c));
        }
        Log.i("ContactInfo", "setNickname,sortKey:"+sortKey);
        this.nickname = nickname;
    }

    public String getSortKey() {
        return sortKey;
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                "id='" + id + '\'' +
                ", contactId='" + contactId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", sortKey='" + sortKey + '\'' +
                '}';
    }
}
