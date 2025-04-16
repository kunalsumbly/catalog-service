package com.polarbookshop.catalogservice;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String getGreeting() {
        System.out.println("Hit ME !!!");
        return "Welcome to book catalog service";
    }
}
