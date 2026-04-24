package com.example.mail.dto;

import com.example.mail.model.Case;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PrenotazioneDTO {
	private UUID id;
	private String ospiteNome;
	private LocalDate checkIn;
	private LocalDate checkOut;
	private String emailOspite;
	private String note;
	private String telefonoOspite;
	private BigDecimal prezzoTotale;
	private BigDecimal caparra;
	private OffsetDateTime createdAt;
	private Long casaId;

    
	public Long getCasaId() { return casaId; }
	public void setCasaId(Long casaId) { this.casaId = casaId; }

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }

	public String getOspiteNome() { return ospiteNome; }
	public void setOspiteNome(String ospiteNome) { this.ospiteNome = ospiteNome; }

	public LocalDate getCheckIn() { return checkIn; }
	public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }

	public LocalDate getCheckOut() { return checkOut; }
	public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }

	public String getEmailOspite() { return emailOspite; }
	public void setEmailOspite(String emailOspite) { this.emailOspite = emailOspite; }

	public String getNote() { return note; }
	public void setNote(String note) { this.note = note; }

	public String getTelefonoOspite() { return telefonoOspite; }
	public void setTelefonoOspite(String telefonoOspite) { this.telefonoOspite = telefonoOspite; }

	public BigDecimal getPrezzoTotale() { return prezzoTotale; }
	public void setPrezzoTotale(BigDecimal prezzoTotale) { this.prezzoTotale = prezzoTotale; }

	public BigDecimal getCaparra() { return caparra; }
	public void setCaparra(BigDecimal caparra) { this.caparra = caparra; }

	public OffsetDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
               
