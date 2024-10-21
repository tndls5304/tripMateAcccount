package com.tripmate.account.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@SuperBuilder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass // 이 클래스를 상속받는 모든 엔티티에 해당 컬럼들이 포함되도록 합니다.
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseEntity {

    @Column(name="REG_USER",nullable = false,updatable = false,length = 30)
    String regUser;

    @CreatedDate
    @Column(name = "REG_DTM",nullable = false,updatable = false)
    LocalDateTime regDtm;

    @Column(name = "UPDT_USER",length = 30)
    String updtUser;

    @LastModifiedDate // @LastModifiedDate:엔티티가 마지막으로 수정된 날짜와 시간을 자동으로 기록( 엔티티가 업데이트될 때, 현재 날짜와 시간으로 자동으로 설정) ,LocalDateTime과 함께 사용하는 것이 좋다
    @Column(name="UPDT_DTM")
    LocalDateTime updtDtm;
}
