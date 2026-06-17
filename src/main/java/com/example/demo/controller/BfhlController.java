package com.example.demo.controller;

import com.example.demo.dto.BfhlRequest;
import com.example.demo.dto.BfhlResponse;
import com.example.demo.service.BfhlService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bfhl")
public class BfhlController {

    @Autowired
    private BfhlService bfhlService;

    public BfhlController(BfhlService bfhlService) {
        this.bfhlService = bfhlService;
    }

    @PostMapping
    public ResponseEntity<BfhlResponse> process(
            @RequestHeader(value = "X-Request-Id", required = false) String requestId,
            @Valid @RequestBody BfhlRequest request
    ) {
        BfhlResponse response = bfhlService.process(request, requestId);
        return ResponseEntity.ok()
                .header("X-Request-Id", requestId != null ? requestId : "")
                .body(response);
    }
}