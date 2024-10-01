package com.tripmate.account.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_TB")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)  // Auditing 기능 활성화
public class UserEntity {
    @Id
    @Column(name = "user_id", nullable = false, length = 50)
    String userId;

    @Column(name = "user_pwd", nullable = false)
    String userPwd;

    @Column(name = "nickname", nullable = false, length = 30)
    String nickname;

    @Column(name = "phone_no", nullable = false, length = 11)
    String phoneNo;

    @Column(name = "email_id", nullable = false, length = 30)
    String emailId;

    @Column(name = "email_domain", nullable = false, length = 30)
    String emailDomain;

    @Column(name = "reg_user", nullable = false)
    String regUser;

    @CreatedDate
    @Column(name = "reg_dtm", nullable = false, updatable = false)
    LocalDateTime regDtm;

    @Column(name = "pwd_upd_dt")
    LocalDate pwdUpdDt;

    @Column(name = "updt_user")
    String updtUser;

    @LastModifiedDate
    @Column(name = "updt_dtm")
    LocalDateTime updtDtm;

    @Column(name = "last_login_dt")
    LocalDate lastLoginDt;
}
