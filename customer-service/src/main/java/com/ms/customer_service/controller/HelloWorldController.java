package com.ms.customer_service.controller;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloWorldController{

    @GetMapping(value = "api/customer/hello", produces = {MediaType.TEXT_PLAIN_VALUE})
    public String hello(@RequestHeader(value="X-User") String user ){
        System.out.print(user);
        return "Hello";
    }
}