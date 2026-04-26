package com.example.mail.dto;

import java.time.LocalDate;

public class PrezzoCasaDTO {
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private Integer prezzoTotale;

    public PrezzoCasaDTO(LocalDate dataInizio, LocalDate dataFine, Integer prezzoTotale) {
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.prezzoTotale = prezzoTotale;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public void setDataInizio(LocalDate dataInizio) {
        this.dataInizio = dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public void setDataFine(LocalDate dataFine) {
        this.dataFine = dataFine;
    }

    public Integer getPrezzoTotale() {
        return prezzoTotale;
    }

    public void setPrezzoTotale(Integer prezzoTotale) {
        this.prezzoTotale = prezzoTotale;
    }
}
