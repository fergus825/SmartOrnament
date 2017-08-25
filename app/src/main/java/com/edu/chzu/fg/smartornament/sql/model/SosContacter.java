package com.edu.chzu.fg.smartornament.sql.model;

/**
 * Created by FG on 2017/3/28.
 */

public class SosContacter {
    private int id;
    private String name;
    private String relation;
    private String phoneNumber;
    private String smsContent;
    private int isChecked;
    public SosContacter(){
        super();
    }
    public SosContacter(int id, String name, String relation, String phoneNumber, String smsContent, int isChecked) {
        this.id = id;
        this.name = name;
        this.relation = relation;
        this.phoneNumber = phoneNumber;
        this.smsContent = smsContent;
        this.isChecked=isChecked;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRelation() {
        return relation;
    }
    public void setRelation(String relation) {
        this.relation = relation;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getSmsContent() {
        return smsContent;
    }
    public void setSmsContent(String smsContent) {
        this.smsContent = smsContent;
    }
    public int getIsChecked() {
        return isChecked;
    }
    public void setIsChecked(int isChecked) {
        this.isChecked = isChecked;
    }
}
