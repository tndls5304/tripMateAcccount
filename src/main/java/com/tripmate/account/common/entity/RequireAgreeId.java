package com.tripmate.account.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;

import java.util.Objects;
@Builder
@Embeddable
public class RequireAgreeId {

    @Column(name ="ACCOUNT_TYPE")
    private char accountType;
    @Column(name = "ACCOUNT_ID",length = 30)
    private String accountId;
    @Column(name = "TEMPLATE_SQ")
    private int templateSq;

    public RequireAgreeId(){
    }
    public RequireAgreeId(char userType, String userId, int templateSq){
        this.accountType=userType;
        this.accountId=userId;
        this.templateSq=templateSq;
    }
    public char getUserType(){
        return accountType;
    }
    public String getUserId(){
        return accountId;
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
        return accountType == that.accountType &&
                templateSq == that.templateSq &&
                Objects.equals(accountId, that.accountId);
    }

    // hashCode() 메서드 재정의
    @Override
    public int hashCode() {
        return Objects.hash(accountType, accountId, templateSq);
    }
}

