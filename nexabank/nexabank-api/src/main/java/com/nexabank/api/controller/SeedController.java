package com.nexabank.api.controller;

import com.nexabank.api.service.DataSeederService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/seed")
public class SeedController {

    private final DataSeederService dataSeederService;

    public SeedController(DataSeederService dataSeederService) {
        this.dataSeederService = dataSeederService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> seedDatabase() {
        return ResponseEntity.ok(dataSeederService.seedDatabase());
    }
}
