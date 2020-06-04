package com.example.gcorddemo.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;

public class ContactBean implements Serializable {
    private static final long serialVersionUID = 42L;
    private String givenName;
    private String familyName;
    private ArrayList<PhoneData> phoneDataList = new ArrayList<>();
    private String company;
    private String rawContactId = "";
    private String displayName;
    private String organizationId;
    private String structuredNameId;

    public ContactBean() {
    }

    public ContactBean(@NonNull ContactBean item) {
        this(item.givenName, item.familyName, item.phoneDataList, item.company, item.rawContactId, item.organizationId, item.structuredNameId, item.getDisplayName());
        rawContactId = item.rawContactId;
    }

    public ContactBean(String givenName, String familyName,
                       ArrayList<PhoneData> phoneDataList, String company, String rawContactId, String organizationId, String structuredNameId, String displayName) {
        this.givenName = givenName;
        this.familyName = familyName;

        if (phoneDataList != null) {
            for (PhoneData item :
                    phoneDataList) {
                this.phoneDataList.add(new PhoneData(item));
            }
        }

        this.company = company;
        if (rawContactId == null) rawContactId = "";
        this.rawContactId = rawContactId;
        this.organizationId = organizationId;
        this.structuredNameId = structuredNameId;
        this.setDisplayName(displayName);
    }

    public String getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(String rawContactId) {
        if (rawContactId == null) rawContactId = "";
        this.rawContactId = rawContactId;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }


    public void addPhoneNumber(String dataId, String number) {
        this.phoneDataList.add(new PhoneData(dataId, number));
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ArrayList<PhoneData> getPhoneDataList() {
        return phoneDataList;
    }

    @SuppressWarnings("unused")
    public synchronized void setPhoneDataList(ArrayList<PhoneData> phoneDataList) {
        this.phoneDataList.clear();
        if (phoneDataList != null) {
            for (PhoneData item :
                    phoneDataList) {
                this.phoneDataList.add(new PhoneData(item));
            }
        }
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getStructuredNameId() {
        return structuredNameId;
    }

    public void setStructuredNameId(String structuredNameId) {
        this.structuredNameId = structuredNameId;
    }

    public static class PhoneData implements Serializable {
        private static final long serialVersionUID = 42L;
        private String dataId = null;
        private String phoneNumber;

        public PhoneData(@NonNull PhoneData copy) {
            this(copy.dataId, copy.phoneNumber);
        }

        public PhoneData(String dataId, String phoneNumber) {
            this.dataId = dataId;
            this.phoneNumber = phoneNumber;
        }

        @SuppressWarnings("unused")
        public PhoneData(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getDataId() {
            return dataId;
        }

        @SuppressWarnings("unused")
        public void setDataId(String dataId) {
            this.dataId = dataId;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}
