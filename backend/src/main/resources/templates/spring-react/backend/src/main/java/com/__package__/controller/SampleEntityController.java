package com.__package__.controller;

import com.__package__.entity.SampleEntity;
import com.__package__.service.SampleEntityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sample-entities")
public class SampleEntityController {
    private final SampleEntityService service;
    public SampleEntityController(SampleEntityService service) { this.service = service; }

    @GetMapping
    public List<SampleEntity> getAll() { return service.findAll(); }

    @PostMapping
    public ResponseEntity<SampleEntity> create(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.create(body.get("name"), body.get("description")));
    }
}