package com.example.mail.dto;

import java.util.List;

public class CalendarioDisponibilitaDTO {
    private int giorni;
    private List<String> checkInDisponibili;

    public CalendarioDisponibilitaDTO(int giorni, List<String> checkInDisponibili) {
        this.giorni = giorni;
        this.checkInDisponibili = checkInDisponibili;
    }

    public int getGiorni() {
        return giorni;
    }

    public List<String> getCheckInDisponibili() {
        return checkInDisponibili;
    }
}