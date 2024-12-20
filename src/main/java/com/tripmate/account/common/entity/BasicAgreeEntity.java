package com.tripmate.account.common.entity;

import com.tripmate.account.common.entity.compositekey.BasicAgreeId;
import com.tripmate.account.common.entity.base.BaseEntity;
import com.tripmate.account.common.enums.AgreeFl;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;


/**
 * 필수 약관동의 엔티티
 *
 * @author 이수인
 * @since 2024.10.13
 */

@Entity
@Table(name = "BASIC_AGREE_TH")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)

public class BasicAgreeEntity extends BaseEntity {
    //복합키
//    char userType;
//    String userId;
//    int templateSq;
    @EmbeddedId
    BasicAgreeId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "AGREE_FL", nullable = false, columnDefinition = "ENUM('Y', 'N') DEFAULT 'Y'") // ENUM 타입으로 정의
    AgreeFl agreeFl;

    @Column(name = "AGREE_DT", nullable = false)
    LocalDate agreeDt;
    //-----------------수정에 관한것도 상속받는중인데 어떡하지---------
//    @Column(name = "REG_USER",nullable = false)
//    char regUser;
//    @CreatedDate
//    @Column(name = "REG_DTM",nullable = false)
//    LocalDateTime regDtm;

}
