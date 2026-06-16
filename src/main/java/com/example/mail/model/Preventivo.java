package com.example.mail.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "preventivo")
public class Preventivo {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "appartamento")
    private String appartamento;

    @Column(name = "check_in")
    private LocalDate checkIn;

    @Column(name = "check_out")
    private LocalDate checkOut;

    @Column(name = "persone")
    private Integer persone;

    @Column(name = "prezzo")
    private BigDecimal prezzo;

    @Column(name = "messaggio", columnDefinition = "text")
    private String messaggio;

    @Column(name = "preferenza_ricontatto")
    private String preferenzaRicontatto;

    @Column(name = "source")
    private String source;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getAppartamento() { return appartamento; }
    public void setAppartamento(String appartamento) { this.appartamento = appartamento; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public Integer getPersone() { return persone; }
    public void setPersone(Integer persone) { this.persone = persone; }

    public BigDecimal getPrezzo() { return prezzo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }

    public String getMessaggio() { return messaggio; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }

    public String getPreferenzaRicontatto() { return preferenzaRicontatto; }
    public void setPreferenzaRicontatto(String preferenzaRicontatto) { this.preferenzaRicontatto = preferenzaRicontatto; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
