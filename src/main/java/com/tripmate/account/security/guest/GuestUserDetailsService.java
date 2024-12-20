package com.tripmate.account.security.guest;

import com.tripmate.account.common.entity.GuestEntity;
import com.tripmate.account.common.enums.AccountType;
import com.tripmate.account.common.enums.RoleCode;
import com.tripmate.account.guest.repository.GuestRoleThRepository;
import com.tripmate.account.guest.repository.GuestTbRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 숙박이용자의 UserDetailsService를  구현한 클래스입니다.
 * UsernamePasswordAuthenticationToken 토큰에 있는 사용자 아이디를 전달받아 해당 아이디로 데이터베이스에서 사용자를 조회하고, 조회된 정보를 UserDetails 객체로 반환하는 역할
 * 마지막으로 AuthenticationProvider는 반환된 UserDetails 객체의 비밀번호와 사용자가 입력한 비밀번호를 비교하여 인증을 완료합니다.
 */

@Service
public class GuestUserDetailsService implements UserDetailsService {

    private final GuestTbRepository repository;
    private final GuestRoleThRepository guestRoleThRepository;

    public GuestUserDetailsService(GuestTbRepository repository, GuestRoleThRepository guestRoleThRepository) {
        this.repository = repository;
        this.guestRoleThRepository = guestRoleThRepository;
    }

    /**
     * 시큐리티 provider인 DaoAuthenticationProvider에서 호출하는 메서드다.
     * 이 메서드는 클라이언트가 입력한 id로 계정을 조회한 후 UserDetails 타입의 객체로 반환하는 역할을 한다.
     *
     * @param userId 클라이언트가 입력한 사용자 ID를 나타낸다.
     * @return UserDetails을 구현한 GuestUserDetails  객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 경우 발생
     */


    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        // 1. RoleCode 리스트를 조회합니다.
        GuestEntity guestEntity = repository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 2. RoleCode 리스트를 이용해 GrantedAuthority 객체 생성
        Set<GrantedAuthority> authoritySet = new HashSet<>();
        //권한 코드는 리스트로 빼와서  SimpleGrantedAuthority의 생성자의 인자값으로 넘겨주면 String 멤버변수에 저장
        //Set에는
        List<RoleCode> roleCodeList = guestRoleThRepository.findRoleCodeByUserTypeAndId(AccountType.G, guestEntity.getUserId());
        for (RoleCode roleCode : roleCodeList) {
            authoritySet.add(new SimpleGrantedAuthority(roleCode.name())); //이늄타입이라 String을 얻기위해 name()을 사용함  RoleCode.RG00이면 RG00이라는 문자열이 반환됩니다.
        }

        return new GuestUserDetails(
                guestEntity.getUserId(),
                guestEntity.getUserPwd(),
                authoritySet // 권한을 Set으로 추가
        );
    }
}


      /*  Optional<GuestEntity> userEntityOptional = repository.findById(userId);

        if (userEntityOptional.isEmpty()) {
            throw new UsernameNotFoundException("User ID not found: " + userId);
        }
       */