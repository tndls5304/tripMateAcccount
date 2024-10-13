package com.tripmate.account.common.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;


/**
 *숙박객, 호스트의 필수 기본 약관동의 엔티티
 * @author 이수인
 * @since 2024.10.13
 */
@Entity
@Table(name="TERMS_AGREE_TH")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)

public class TermsAgreeEntity extends BaseEntity {

//    char userType;
//    String userId;
//    int templateSq;
    @EmbeddedId
    TermsAgreeId id;
    @Column(name = "AGREE_FL",nullable = false,columnDefinition = "CHAR(1) DEFAULT 'Y'")
    char agreeFl;
    @CreatedDate
    @Column(name ="AGREE_DT",nullable = false)
    LocalDate agreeDt;
    //-----------------수정에 관한것도 상속받는중인데 어떡하지---------
//    @Column(name = "REG_USER",nullable = false)
//    char regUser;
//    @CreatedDate
//    @Column(name = "REG_DTM",nullable = false)
//    LocalDateTime regDtm;

}
