package com.tripmate.account.common.entity;

import jakarta.persistence.Embeddable;

import java.util.Objects;

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
    // equals() 메서드 재정의-----------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermsAgreeId that = (TermsAgreeId) o;
        return userType == that.userType &&
                templateSq == that.templateSq &&
                Objects.equals(userId, that.userId);
    }

    // hashCode() 메서드 재정의
    @Override
    public int hashCode() {
        return Objects.hash(userType, userId, templateSq);
    }
}

