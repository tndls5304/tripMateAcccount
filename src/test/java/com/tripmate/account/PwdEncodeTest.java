package com.tripmate.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PwdEncodeTest {
    @Autowired
    PasswordEncoder passwordEncoder;//DI

    @Test
    void pwdEnc() {
        String originPwd = "12345";
        String encodedPwd = passwordEncoder.encode(originPwd);
        System.out.println(encodedPwd);
    }

    @Test
    void pwdMatch() {
        // 기존 저장해두었던 암호화된 비밀번호
        String encodedPwd = "{bcrypt}$2a$10$xO99cg0RupsQY4PNvdPJe.neRL7JSplM8t/NQUgBRGnOM19/FbstS";
        // 검증할 비밀번호
        String originPwd = "kedric123";

        if (passwordEncoder.matches(originPwd, encodedPwd)) {
            System.out.println("true");
        }else{
            System.out.println("false");
        }

    }

}
