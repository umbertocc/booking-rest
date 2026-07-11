package com.example.mail.controller;

import com.example.mail.model.Preventivo;
import com.example.mail.repository.PreventivoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/preventivi")
public class AdminPreventivoController {

    private final PreventivoRepository preventivoRepository;

    public AdminPreventivoController(PreventivoRepository preventivoRepository) {
        this.preventivoRepository = preventivoRepository;
    }

    @GetMapping
    public List<Preventivo> listPreventivi() {
        return preventivoRepository.findAllByOrderByCreatedAtDesc();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePreventivo(@PathVariable("id") UUID id) {
        if (!preventivoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        preventivoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 