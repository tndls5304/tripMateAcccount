package com.tripmate.account.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 마케팅(선택) 약관동의 엔티티
 *
 * @author 이수인
 * @since 2024.10.17
 */
@Entity
@Table(name = "MARKETING_AGREE_TH")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class MarketingAgreeEntity extends BaseEntity {
    @Id
    @Column(name = "AGREE_SQ", length = 39, nullable = false)//날짜시간(14자)+계정타입+ID(20자)+SQ(4자리)
    String agreeSq;
    @Column(name = "ACCOUNT_TYPE", nullable = false)
    char accountType;
    @Column(name = "ACCOUNT_ID", length = 30, nullable = false)
    String accountId;
    @Column(name = "AGREE_FL", nullable = false)
    char agreeFl;
    @CreatedDate
    @Column(name = "AGREE_DTM")
    LocalDateTime agreeDtm;
    @Column(name = "D_AGREE_DTM")
    LocalDateTime dAgreeDtm;
    @Column(name = "TEMPLATE_SQ", nullable = false)
    int templateSq;

}
/*
PK구성
    날짜 및 시간 (14자): yyyyMMddHHmmss (년월일시분초, 14자)
    계정 타입 (1자): H 또는 A
    아이디 (최대 20자)
    시퀀스 (4자리): 0001~9999 (중복 방지)
    총 길이 계산: 14 + 1 + 20 + 4 = 39자
 */
