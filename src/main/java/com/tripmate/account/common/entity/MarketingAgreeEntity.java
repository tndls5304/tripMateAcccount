package com.tripmate.account.common.entity;

import com.tripmate.account.common.entity.base.BaseEntity;
import com.tripmate.account.common.enums.AccountType;
import com.tripmate.account.common.enums.AgreeFl;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 마케팅(선택) 약관동의 엔티티
 *
 * @author 이수인
 * @since 2024.10.17
 */
@ToString
@Entity
@Table(name = "MARKETING_AGREE_TH")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class MarketingAgreeEntity extends BaseEntity {
    @Id
    @Column(name = "AGREE_SQ", nullable = false)//길이 정해지면 넣기 ⭐⭐
    String agreeSq;

    @Enumerated(EnumType.STRING)
    @Column(name = "ACCOUNT_TYPE", nullable = false)
    AccountType accountType;

    @Column(name = "ACCOUNT_ID", length = 30, nullable = false)
    String accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "AGREE_FL", nullable = false)
    AgreeFl agreeFl;

    @Column(name = "AGREE_DTM")
    LocalDateTime agreeDtm;

    @Column(name = "D_AGREE_DTM")
    LocalDateTime dAgreeDtm;

    @Column(name = "TEMPLATE_SQ", nullable = false)
    int templateSq;
}
