package com.matvey.cinema.controllers;

import com.matvey.cinema.service.VisitCounterService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/visits")
public class VisitCounterController {

    private final VisitCounterService visitCounterService;

    @Autowired
    public VisitCounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getVisitCount(@RequestParam String url) {
        String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
        int count = visitCounterService.getVisitCount(decodedUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("url", decodedUrl);
        response.put("count", count);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}