package com.tripmate.account.security;

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
 * UsernamePasswordAuthenticationToken 토큰에 있는 아이디를 전달받아 해당 아이디로 데이터베이스에서 사용자를 조회하고, 조회된 정보를 UserDetails 객체로 반환하는 역할
 * 마지막으로 AuthenticationProvider는 반환된 UserDetails 객체의 비밀번호와 사용자가 입력한 비밀번호를 비교하여 인증을 완료합니다.
 *
 */
@Service
public class GeneralUserDetailsService implements UserDetailsService {

    private final UserTbRepository repository;

    public GeneralUserDetailsService(UserTbRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Optional<UserEntity> userEntityOptional = repository.findById(userId);

        if (userEntityOptional.isEmpty()) {
            throw new UsernameNotFoundException("User ID not found: " + userId);
        }

        UserEntity idPassUserEntity = userEntityOptional.get();

        // UserDetails 객체로 변환하여 반환
        return new org.springframework.security.core.userdetails.User(
                idPassUserEntity.getUserId(),       // 사용자 아이디
                idPassUserEntity.getUserPwd(),     // 사용자 비밀번호
                idPassUserEntity.getRoles()    // 사용자 권한 리스트
        );
    }

    // 권한 정보를 설정하는 메서드 예시
    private Collection<? extends GrantedAuthority> getAuthorities(UserEntity userEntity) {
        return userEntity.getRoles().stream()
                .map(Role::getAuthority)  // getAuthority() 메서드를 사용해 권한을 설정
                .map(SimpleGrantedAuthority::new) // 권한 문자열을 SimpleGrantedAuthority로 변환
                .collect(Collectors.toList());
    }
}