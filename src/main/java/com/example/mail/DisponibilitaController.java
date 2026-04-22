package com.example.mail;

import com.example.mail.model.Case;
import com.example.mail.service.DisponibilitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilita")
public class DisponibilitaController {
    @Autowired
    private DisponibilitaService disponibilitaService;

    @GetMapping("/case")
    public List<Case> getCaseDisponibili(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return disponibilitaService.getCaseDisponibili(checkIn, checkOut);
    }
}
