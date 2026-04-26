package com.example.mail.controller;

import com.example.mail.dto.PrezzoCasaDTO;
import com.example.mail.service.PrezzoCasaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/admin/prezzi-case")
public class AdminPrezzoCasaController {
    @Autowired
    private PrezzoCasaService prezzoCasaService;

    @GetMapping
    public List<PrezzoCasaDTO> getPrezziCase(@org.springframework.web.bind.annotation.RequestParam(value = "casaId", required = false) Long casaId) {
        if (casaId != null) {
            return prezzoCasaService.getPrezziCaseByCasaId(casaId);
        } else {
            return prezzoCasaService.getPrezziCase();
        }
    }

    @GetMapping("/by-casa")
    public List<PrezzoCasaDTO> getPrezziCaseByCasaId(@org.springframework.web.bind.annotation.RequestParam("casaId") Long casaId) {
        return prezzoCasaService.getPrezziCaseByCasaId(casaId);
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public void deletePrezzoCasa(@org.springframework.web.bind.annotation.PathVariable Long id) {
        prezzoCasaService.deletePrezzoCasa(id);
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public void updatePrezzoCasa(@org.springframework.web.bind.annotation.PathVariable Long id,
                                 @org.springframework.web.bind.annotation.RequestBody com.example.mail.dto.PrezzoCasaDTO dto) {
        prezzoCasaService.updatePrezzoCasa(id, dto);
    }

    @org.springframework.web.bind.annotation.PostMapping
    public void addPrezzoCasa(@org.springframework.web.bind.annotation.RequestBody com.example.mail.dto.PrezzoCasaDTO dto) {
        prezzoCasaService.addPrezzoCasa(dto);
    }
}
