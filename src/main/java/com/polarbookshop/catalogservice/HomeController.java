package com.polarbookshop.catalogservice;

import com.polarbookshop.catalogservice.config.PolarProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HomeController {
    private final PolarProperties polarProperties;

    public HomeController(PolarProperties polarProperties) {
        this.polarProperties = polarProperties;
    }

    @GetMapping("/")
    public String getGreeting() {
        log.info("Hit ME info!!! controller called ::::");
        log.warn("Hit ME warn!!! controller called ::::");
        log.debug("Hit ME debug !!! controller called ::::");
        log.error("Hit ME error!!! controller called ::::");
        log.trace("Hit ME trace !!! controller called ::::");
        return polarProperties.getGreeting();
    }
}
