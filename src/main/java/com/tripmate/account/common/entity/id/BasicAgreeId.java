package com.tripmate.account.common.entity.id;

import com.tripmate.account.common.enums.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;

import java.util.Objects;
@Builder
@Embeddable
public class BasicAgreeId {

    @Column(name ="ACCOUNT_TYPE")
    private AccountType accountType;
    @Column(name = "ACCOUNT_ID",length = 30)
    private String accountId;
    @Column(name = "TEMPLATE_SQ")
    private int templateSq;

    public BasicAgreeId(){
    }
    public BasicAgreeId(AccountType userType, String userId, int templateSq){
        this.accountType=userType;
        this.accountId=userId;
        this.templateSq=templateSq;
    }
    public AccountType getUserType(){
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
        BasicAgreeId that = (BasicAgreeId) o;
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

