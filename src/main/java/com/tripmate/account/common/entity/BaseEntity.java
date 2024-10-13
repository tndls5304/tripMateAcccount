package com.tripmate.account.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 이 클래스를 상속받는 모든 엔티티에 해당 컬럼들이 포함되도록 합니다.
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseEntity {

    @Column(name="REG_USER",nullable = false,updatable = false)
    String regUser;

    @CreatedDate
    @Column(name = "REG_DTM",nullable = false,updatable = false)
    LocalDateTime regDtm;

    @Column(name = "UPDT_USER")
    String updtUser;

    @LastModifiedDate
    @Column(name="UPDT_DTM")
    LocalDateTime updtDtm;
}
