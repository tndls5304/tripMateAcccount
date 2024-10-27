package com.tripmate.account.common.entity;

import com.tripmate.account.common.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;

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

    @CreatedDate    // LocalDate +@CreatedDate =>  DB에서 DATE 타입으로 가입하는 '날짜'만 저장되도록 자동으로 처리됨
    @Column(name = "PWD_UPD_DT")
    LocalDate pwdUpdDt;

    @Column(name = "LAST_LOGIN_DT")
    LocalDate lastLoginDt;
}
