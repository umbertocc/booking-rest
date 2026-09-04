package com.example.mail.controller;

import com.example.mail.dto.CasaDisponibileDTO;
import com.example.mail.dto.CalendarioDisponibilitaDTO;
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

    @GetMapping("/calendario")
    public CalendarioDisponibilitaDTO getCalendarioDisponibilita(
            @RequestParam(value = "giorni", defaultValue = "540") int giorni) {
        return disponibilitaService.getCalendarioDisponibilita(giorni);
    }

    @GetMapping("/case")
    public List<CasaDisponibileDTO> getCaseDisponibili(
            @RequestParam("checkIn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam("checkOut") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(value = "ospiti", required = false) Integer ospiti) {
        return disponibilitaService.getCaseDisponibili(checkIn, checkOut, ospiti);
    }
}
