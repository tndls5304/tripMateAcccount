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
@MappedSuperclass
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseEntity {

    @Column(name="REG_USER",nullable = false,updatable = false,length = 30)
    String regUser;

    @CreatedDate
    @Column(name = "REG_DTM",nullable = false,updatable = false)
    LocalDateTime regDtm;

    @Column(name = "UPDT_USER",length = 30)
    String updtUser;

   // @LastModifiedDate // @LastModifiedDate:엔티티가 마지막으로 수정된 날짜와 시간을 자동으로 기록( 엔티티가 업데이트될 때, 현재 날짜와 시간으로 자동으로 설정) ,LocalDateTime과 함께 사용하는 것이 좋다
    @Column(name="UPDT_DTM")
    LocalDateTime updtDtm;
}

/*
@MappedSuperclass
목적: @MappedSuperclass 애너테이션은 해당 클래스를 상속받는 모든 엔티티에 대해 필드와 매핑 정보를 상속할 수 있도록 합니다.
특징:
이 클래스는 직접 엔티티로 사용될 수 없으며, 반드시 다른 엔티티 클래스에서 상속받아야 합니다.
상속받는 엔티티 클래스는 부모 클래스의 필드, 메서드, 애너테이션을 사용할 수 있습니다.
데이터베이스 테이블에 직접 매핑되지 않고, 자식 클래스의 테이블에 필드가 포함됩니다.
 */