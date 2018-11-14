package com.kinstalk.her.cmccmode.data;

import java.util.List;

public class AllContactsResponse {
    private int recode;
    private String desc;
    private AllContactsResult result;

    public AllContactsResponse() {
    }

    public AllContactsResponse(int recode, String desc, AllContactsResult result) {
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

    public AllContactsResult getResult() {
        return result;
    }

    public void setResult(AllContactsResult result) {
        this.result = result;
    }

    public List<ContactInfo> getAllContacts() {
        if (recode == 1) {
            return result.contactList;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AllContactsResponse{" +
                "recode=" + recode +
                ", desc='" + desc + '\'' +
                ", result=" + result +
                '}';
    }

    public class AllContactsResult {
        private List<ContactInfo> contactList;

        public AllContactsResult(List<ContactInfo> contactList) {
            this.contactList = contactList;
        }

        public AllContactsResult() {
        }

        public List<ContactInfo> getContactList() {
            return contactList;
        }

        public void setContactList(List<ContactInfo> contactList) {
            this.contactList = contactList;
        }

        @Override
        public String toString() {
            return "AllContactsResult{" +
                    "contactList=" + contactList +
                    '}';
        }
    }
}
