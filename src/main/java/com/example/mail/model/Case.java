package com.example.mail.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cases")
public class Case {
    @Id
    private Long id;

    private OffsetDateTime created_at;
    private String nome;
    private Long prezzo_notte;
    private Boolean disponibile;
    private String descrizione;
    private String caratteristiche; 
    private String sottotitolo;
    private String immagine;
    private String linkDettaglio;
 
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLinkDettaglio() { return linkDettaglio; }
    public void setLinkDettaglio(String linkDettaglio) { this.linkDettaglio = linkDettaglio; }

    public String getImmagine() { return immagine; }
    public void setImmagine(String immagine) { this.immagine = immagine; }

    public String getCaratteristiche() { return caratteristiche; }
    public void setCaratteristiche(String caratteristiche) { this.caratteristiche = caratteristiche; }

    public String getSottotitolo() { return sottotitolo; }
    public void setSottotitolo(String sottotitolo) { this.sottotitolo = sottotitolo; }

    public OffsetDateTime getCreated_at() { return created_at; }
    public void setCreated_at(OffsetDateTime created_at) { this.created_at = created_at; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Long getPrezzo_notte() { return prezzo_notte; }
    public void setPrezzo_notte(Long prezzo_notte) { this.prezzo_notte = prezzo_notte; }

    public Boolean getDisponibile() { return disponibile; }
    public void setDisponibile(Boolean disponibile) { this.disponibile = disponibile; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
}
