package com.springtutorials.spring_mvc.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

    @GetMapping("/hello")
    public String callHelloGet() {
        return "GET: Hello from TestRestController";
    }

    @PostMapping("/hello")
    public String callHelloPost() {
        return "POST: Hello from TestRestController";
    }
}
