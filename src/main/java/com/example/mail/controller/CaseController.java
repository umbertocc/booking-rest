package com.example.mail.controller;

import com.example.mail.model.Case;
import com.example.mail.repository.CaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.mail.util.JwtUtil;

@RestController
@RequestMapping("/api/case")
public class CaseController {
    @Autowired
    private CaseRepository caseRepository;

    @GetMapping
    public List<Case> getAll(@RequestHeader("Authorization") String authHeader) {
        // Estrai il token dall'header Authorization
        String token = authHeader.replace("Bearer ", "");
        String email = JwtUtil.getUsernameFromToken(token);
        return caseRepository.findByEmail(email);
    }
}
