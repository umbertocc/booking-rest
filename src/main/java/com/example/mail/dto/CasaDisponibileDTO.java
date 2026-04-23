package com.example.mail.dto;

import com.example.mail.model.Case;
import java.math.BigDecimal;

public class CasaDisponibileDTO {
    private Case casa;
    private BigDecimal prezzoTotale;

    public CasaDisponibileDTO(Case casa, BigDecimal prezzoTotale) {
        this.casa = casa;
        this.prezzoTotale = prezzoTotale;
    }

    public Case getCasa() { return casa; }
    public void setCasa(Case casa) { this.casa = casa; }

    public BigDecimal getPrezzoTotale() { return prezzoTotale; }
    public void setPrezzoTotale(BigDecimal prezzoTotale) { this.prezzoTotale = prezzoTotale; }
}
