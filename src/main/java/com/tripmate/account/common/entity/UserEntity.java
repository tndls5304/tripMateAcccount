package com.tripmate.account.common.entity;

import com.tripmate.account.common.entity.base.BaseEntity;
import com.tripmate.account.security.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "USER_TB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)  // JPA Auditing  기능 활성화
public class UserEntity extends BaseEntity {
    
    @Id
    @Column(name = "USER_ID", length = 20)
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

    @Column(name = "PWD_UPD_DT")
    LocalDate pwdUpdDt;

    @Column(name = "LAST_LOGIN_DT")
    LocalDate lastLoginDt;

    //권한가져오기-------------------------------------------------------
    private Set<Role> roles; // 중복된 권한을 허용하지 않기 때문에, 같은 권한이 여러 번 추가되는 상황을 방지할 수 있습니다.

    public Set<Role> getRoles() {
        return roles;
    }
}
