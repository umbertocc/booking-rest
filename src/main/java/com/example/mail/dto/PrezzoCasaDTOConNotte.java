package com.example.mail.dto;

import java.time.LocalDate;

public class PrezzoCasaDTOConNotte extends PrezzoCasaDTO {
    private Integer prezzoNotte;

    public PrezzoCasaDTOConNotte(LocalDate dataInizio, LocalDate dataFine, Integer prezzoTotale, Integer prezzoNotte) {
        super(dataInizio, dataFine, prezzoTotale);
        this.prezzoNotte = prezzoNotte;
    }

    public Integer getPrezzoNotte() {
        return prezzoNotte;
    }

    public void setPrezzoNotte(Integer prezzoNotte) {
        this.prezzoNotte = prezzoNotte;
    }
}
