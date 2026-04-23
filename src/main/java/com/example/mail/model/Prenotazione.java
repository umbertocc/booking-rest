package com.example.mail.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "prenotazioni")
public class Prenotazione {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "casa_id")
    private Long casaId;

    @Column(name = "ospite_nome", nullable = false)
    private String ospiteNome;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(name = "email_ospite")
    private String emailOspite;

    @Column(name = "telefono_ospite")
    private String telefonoOspite;

    @Column(name = "prezzo_totale")
    private BigDecimal prezzoTotale;


    @Column(name = "caparra")
    private BigDecimal caparra;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Long getCasaId() { return casaId; }
    public void setCasaId(Long casaId) { this.casaId = casaId; }

    public String getOspiteNome() { return ospiteNome; }
    public void setOspiteNome(String ospiteNome) { this.ospiteNome = ospiteNome; }

    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

    public String getEmailOspite() { return emailOspite; }
    public void setEmailOspite(String emailOspite) { this.emailOspite = emailOspite; }

    public String getTelefonoOspite() { return telefonoOspite; }
    public void setTelefonoOspite(String telefonoOspite) { this.telefonoOspite = telefonoOspite; }

    public BigDecimal getPrezzoTotale() { return prezzoTotale; }
    public void setPrezzoTotale(BigDecimal prezzoTotale) { this.prezzoTotale = prezzoTotale; }

    public BigDecimal getCaparra() { return caparra; }
    public void setCaparra(BigDecimal caparra) { this.caparra = caparra; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
