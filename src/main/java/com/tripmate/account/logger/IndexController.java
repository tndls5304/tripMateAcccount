package com.tripmate.account.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class IndexController {
    @GetMapping("/")
    public String index(){
        // 해당 url을 호출하면 INFO보다 INFO를 포함한 상위 Log level만 console에 찍히는 것을 알 수 있습니다.
        log.trace("TRACE!!");
        log.debug("DEBUG!!");
        log.info("INFO!!");
        log.warn("WARN!!");
        log.error("ERROR!!");

        return "index";
    }
}
