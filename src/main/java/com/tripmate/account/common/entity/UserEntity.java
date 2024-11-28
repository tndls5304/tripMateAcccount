package com.tripmate.account.common.entity;

import com.tripmate.account.common.entity.base.BaseEntity;
import com.tripmate.account.common.enums.AccountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "USER_TB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing  기능 활성화
public class UserEntity extends BaseEntity {

    @Id
    @Column(name = "USER_ID", length = 20)
    String userId;

    @Column(name = "USER_PWD", nullable = false)
    String userPwd;

    @Column(name = "NICKNAME", nullable = false, length = 30)
    String nickname;

    @Column(name = "PHONE_NO", nullable = false, length = 11)
    String phoneNo;

    @Column(name = "EMAIL_ID", nullable = false, length = 30)
    String emailId;

    @Column(name = "EMAIL_DOMAIN", nullable = false, length = 30)
    String emailDomain;

    @Column(name = "PWD_UPD_DT")
    LocalDate pwdUpdDt;

    @Column(name = "LAST_LOGIN_DT")
    LocalDate lastLoginDt;
    //-----추가
    @Column(name = "CLIENT_TYPE", nullable = false)
    AccountType accountType;


    @PrePersist//엔티티가 저장되기 전에 호출됩니다.
    public void setDefaultClientType() {
        if (this.accountType == null) { // accountType이 null이라면
            this.accountType = AccountType.G ;// 기본값 'G'를 설정
        }
    }
}
