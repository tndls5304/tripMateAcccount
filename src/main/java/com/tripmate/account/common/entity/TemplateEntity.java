package com.tripmate.account.common.entity;

import com.tripmate.account.common.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name="TEMPLATE_TB")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class TemplateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//MySQL에서 기본 키 필드가 AUTO_INCREMENT로 설정
    @Column(name="TEMPLATE_SQ",nullable = false)
    int templateSq;
    @Column(name = "M_CATEGORY",nullable = false)
    char mCategory;
    @Column(name="S_CATEGORY",nullable = false,length = 2)
    String sCategory;
    @Column(name="TITLE",nullable = false,length = 90)
    String title;
    @Column(name = "CONTENT",nullable = false,length = 300)
    String content;
    @Column(name="USE_FL",nullable = false,columnDefinition="CHAR(1) DEFAULT 'Y'")//@PrePersist가 아닌 columnDefinition으로 쓴 이유: JPA뿐만 아니라 데이터베이스에서 직접 쿼리를 실행하여 삽입할 때도 기본값이 적용
    char useFl;
//    @Column(name="REG_USER",nullable = false)
//    String regUser;
//    @CreatedDate
//    @Column(name = "REG_DTM",nullable = false,updatable = false)
//    LocalDateTime regDtm;
//    @Column(name = "UPDT_USER")
//    String updtUser;
//    @LastModifiedDate
//    @Column(name="UPDT_DTM",nullable = true)
//    LocalDateTime updtDtm;
    }

