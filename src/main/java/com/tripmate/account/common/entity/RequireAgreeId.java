package com.tripmate.account.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class RequireAgreeId {
    @Column(name ="USER_TYPE")
    private char userType;
    @Column(name = "USER_ID",length = 30)
    private String userId;
    @Column(name = "TEMPLATE_SQ")
    private int templateSq;

    public RequireAgreeId(){
    }
    public RequireAgreeId(char userType, String userId, int templateSq){
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
        RequireAgreeId that = (RequireAgreeId) o;
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

