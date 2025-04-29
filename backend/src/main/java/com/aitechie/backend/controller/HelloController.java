package com.aitechie.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
public class HelloController {

    // Apply CORS to a specific method
    @CrossOrigin(origins = "http://localhost:60403/")  // Allow frontend at this origin
    @GetMapping("/api/hello")
    public String hello() {
        return "Hello Yogesh,I love u - Backend!";
    }
}


