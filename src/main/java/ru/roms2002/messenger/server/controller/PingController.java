package ru.roms2002.messenger.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class PingController {

    @GetMapping
    public String testRoute() {
        return "Server status OK";
    }
}
