package com.kapotiabi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/testapi/hello")
public class HelloController {

    @GetMapping
    public String hello() {
        return "Hello from the backend";
    }

    @PostMapping
    public String echo(@RequestBody String message) {
        return "Hello, " + message + "!";
    }
}
