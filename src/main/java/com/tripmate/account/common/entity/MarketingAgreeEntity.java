package com.tripmate.account.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 마케팅(선택) 약관동의 엔티티
 * @author 이수인
 * @since  2024.10.17
 */
@Entity
@Table(name = "MARKETING_AGREE_TH")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class MarketingAgreeEntity extends BaseEntity{
    @Id
    @Column(name="AGREE_SQ")
    long agree_sq;
    @Column(name="USER_TYPE",nullable = false)
    char user_type;
    @Column(name = "USER_ID",length = 30,nullable = false)
    String user_id;
    @Column(name = "AGREE_FL",nullable = false)
    char agree_fl;
    @Column(name = "AGREE_DT")
    LocalDateTime agree_dt;
    @Column(name = "D_AGREE_DT")
    LocalDateTime dAgree_dt;
    @Column(name ="TEMPLATE_SQ",nullable = false)
    int templateSq;

}
