package com.example.mail.dto;

import java.time.LocalDate;

public class PrezzoCasaDTO {
    private Long id;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private Integer prezzoTotale;
    private Integer prezzoNotte;

    public PrezzoCasaDTO(Long id, LocalDate dataInizio, LocalDate dataFine, Integer prezzoNotte, Integer prezzoTotale) {
        this.id = id;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.prezzoNotte = prezzoNotte;
        this.prezzoTotale = prezzoTotale;
    }

    public PrezzoCasaDTO(LocalDate dataInizio, LocalDate dataFine, Integer prezzoNotte, Integer prezzoTotale) {
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.prezzoNotte = prezzoNotte;
        this.prezzoTotale = prezzoTotale;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getPrezzoNotte() {
        return prezzoNotte;
    }

    public void setPrezzoNotte(Integer prezzoNotte) {
        this.prezzoNotte = prezzoNotte;
    }
}
