package com.tripmate.account.security;

import com.tripmate.account.common.entity.RoleEntity;
import com.tripmate.account.common.entity.UserEntity;
import com.tripmate.account.user.repository.UserTbRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 숙박이용자의 UserDetailsService를  구현한 클래스입니다.
 * UsernamePasswordAuthenticationToken 토큰에 있는 사용자 아이디를 전달받아 해당 아이디로 데이터베이스에서 사용자를 조회하고, 조회된 정보를 UserDetails 객체로 반환하는 역할
 * 마지막으로 AuthenticationProvider는 반환된 UserDetails 객체의 비밀번호와 사용자가 입력한 비밀번호를 비교하여 인증을 완료합니다.
 */

@Service
public class GeneralUserDetailsService implements UserDetailsService {

    private final UserTbRepository repository;

    public GeneralUserDetailsService(UserTbRepository repository) {
        this.repository = repository;
    }
//DaoAuthenticationProvider에서 호출함
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<UserEntity> userEntityOptional = repository.findById(userId);

        if (userEntityOptional.isEmpty()) {
            throw new UsernameNotFoundException("User ID not found: " + userId);
        }

        UserEntity idPassUserEntity = userEntityOptional.get();

        // UserDetails 객체로 변환하여 반환
        return new UserLoginEntity(
                idPassUserEntity
        );
    }

    /**
     *userEntity의 getRoleEntities() 메서드를 통해 사용자의 권한(예: ROLE_ADMIN, ROLE_USER 등)을 가져오고, 이를 시큐리티가 권한을 비교할때 사용하는 SimpleGrantedAuthority 객체로 변환해 반환합니다.
     * map(RoleEntity::getAuthority)는 각 RoleEntity 객체에서 권한 문자열을 가져오고, 그 문자열을 SimpleGrantedAuthority 객체로 변환해 최종적으로 권한 목록을 생성합니다
     * @param userEntity
     * @return
     */
    private Collection<? extends GrantedAuthority> getAuthorities(UserEntity userEntity) {
        return userEntity.getRoleEntities().stream()
                .map(RoleEntity::getAuthority)//getAuthority() 메서드를 호출하여 각 역할에서 권한 문자열을 가져옵니다."ROLE_ADMIN" 또는 "ROLE_USER"와 같은 문자열을 반환한다고 가정할 수 있습니다.
                .map(SimpleGrantedAuthority::new) // 권한 문자열을 SimpleGrantedAuthority로 변환
                .collect(Collectors.toList());
    }
}

