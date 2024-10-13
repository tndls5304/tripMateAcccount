package com.tripmate.account.common.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class TermsAgreeId {
    private char userType;
    private String userId;
    private int templateSq;

    public TermsAgreeId(){
    }
    public TermsAgreeId(char userType,String userId,int templateSq){
        this.userType=userType;
        this.userId=userId;
        this.templateSq=templateSq;
    }
    public char getUserType(){
        return userType;
    }
    public String getUserId(){
        return userId;
    }
    public int getTemplateSq(){
        return templateSq;
    }
}

