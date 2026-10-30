package com.example.student.controller;

import com.example.student.model.BakingRequest;
import com.example.student.service.BakingRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/baking-requests")
public class BakingRequestController {

    private final BakingRequestService service;

    public BakingRequestController(BakingRequestService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<BakingRequest>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BakingRequest> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<BakingRequest> create(@RequestBody BakingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BakingRequest> update(@PathVariable Long id,
                                                @RequestBody BakingRequest patch) {
        return ResponseEntity.ok(service.update(id, patch));
    }

    @PreAuthorize("hasRole('ADMIN') or @bakingRequestSecurity.isOwner(#id, authentication)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}