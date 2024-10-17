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
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing  기능 활성화
public class UserEntity {
    
    @Id
    @Column(name = "USER_ID", length = 50)
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

    @Column(name = "REG_USER", nullable = false)
    String regUser;

    @CreatedDate    // @CreatedDate:엔티티가 처음 생성될 때 현재 날짜와 시간을 자동으로 저장됨 LocalDateTime과 같이 쓰이면 db에 DATETIME 또는 TIMESTAMP 타입으로 저장됨
    @Column(name = "REG_DTM", nullable = false, updatable = false)
    LocalDateTime regDtm;

    @CreatedDate    // LocalDate +@CreatedDate =>  DB에서 DATE 타입으로 가입하는 '날짜'만 저장되도록 자동으로 처리됨
    @Column(name = "PWD_UPD_DT")
    LocalDate pwdUpdDt;

    @Column(name = "UPDT_USER")
    String updtUser;

    @LastModifiedDate // @LastModifiedDate:엔티티가 마지막으로 수정된 날짜와 시간을 자동으로 기록( 엔티티가 업데이트될 때, 현재 날짜와 시간으로 자동으로 설정) ,LocalDateTime과 함께 사용하는 것이 좋다
    @Column(name = "UPDT_DTM")
    LocalDateTime updtDtm;

    @Column(name = "LAST_LOGIN_DT")
    LocalDate lastLoginDt;
}
