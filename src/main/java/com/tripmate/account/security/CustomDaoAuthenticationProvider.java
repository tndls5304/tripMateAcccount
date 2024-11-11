package com.tripmate.account.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomDaoAuthenticationProvider extends DaoAuthenticationProvider {
    @Autowired
    public CustomDaoAuthenticationProvider (GeneralUserDetailsService service
            , PasswordEncoder passwordEncoder
    ){
        super.setUserDetailsService(service);                                               //--->두개만 셋팅하면 서비스를호출함---> 사용자가 입력한 id로 db조회한 userDetails를 리턴
        super.setPasswordEncoder(passwordEncoder);                                         //암호화된 비밀번호를 비교할려고
    }
}





