package com.example.mail.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prezzi_case")
public class PrezzoCasa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casa_id", nullable = false)
    private Case casa;

    @Column(name = "prezzo_notte", nullable = false)
    private Integer prezzoNotte;

    @Column(name = "inizio_periodo", nullable = false)
    private LocalDate inizioPeriodo;

    @Column(name = "fine_periodo", nullable = false)
    private LocalDate finePeriodo;

    @Column(name = "descrizione")
    private String descrizione;

    @Column(name = "prezzo_totale")
    private Integer prezzoTotale;

    // Getters e setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Case getCasa() { return casa; }
    public void setCasa(Case casa) { this.casa = casa; }

    public Integer getPrezzoNotte() { return prezzoNotte; }
    public void setPrezzoNotte(Integer prezzoNotte) { this.prezzoNotte = prezzoNotte; }

    public LocalDate getInizioPeriodo() { return inizioPeriodo; }
    public void setInizioPeriodo(LocalDate inizioPeriodo) { this.inizioPeriodo = inizioPeriodo; }

    public LocalDate getFinePeriodo() { return finePeriodo; }
    public void setFinePeriodo(LocalDate finePeriodo) { this.finePeriodo = finePeriodo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    
    public Integer getPrezzoTotale() { return prezzoTotale; }
    public void setPrezzoTotale(Integer prezzoTotale) { this.prezzoTotale = prezzoTotale; }
}
