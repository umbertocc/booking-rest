package com.example.mail.controller;

import com.example.mail.util.JwtUtil;
import com.example.mail.repository.CaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminAuthController {
    private final CaseRepository caseRepository;

    public AdminAuthController(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        var adminCases = caseRepository.findByEmailAndPassword(email, password);
        if (adminCases != null && !adminCases.isEmpty()) {
            String token = JwtUtil.generateToken(email);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email o password errata");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
}
