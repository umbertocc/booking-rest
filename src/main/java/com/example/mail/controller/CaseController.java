package com.example.mail.controller;

import com.example.mail.model.Case;
import com.example.mail.model.Prenotazione;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.mail.util.JwtUtil;

@RestController
@RequestMapping("/api/case")
public class CaseController {
    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private PrenotazioneRepository prenotazioneRepository;

    @GetMapping
    public List<Case> getAll(@RequestHeader("Authorization") String authHeader) {
        // Estrai il token dall'header Authorization
        String token = authHeader.replace("Bearer ", "");
        String email = JwtUtil.getUsernameFromToken(token);
        return caseRepository.findByEmail(email);
    }

    // Endpoint pubblico che restituisce tutte le case
    @GetMapping("/public")
    public List<Case> getAllPublic() {
        List<Case> caseList = caseRepository.findAll();
        List<Prenotazione> prenotazioni = prenotazioneRepository.findAll();

        LocalDate oggi = LocalDate.now();
        LocalDate fineFinestra = oggi.plusDays(365);

        Map<Long, Long> giorniDisponibiliPerCasa = new HashMap<>();
        for (Case casa : caseList) {
            long giorniDisponibili = calculateAvailableDays(
                    casa.getId(),
                    prenotazioni,
                    oggi,
                    fineFinestra
            );
            giorniDisponibiliPerCasa.put(casa.getId(), giorniDisponibili);
        }

        caseList.sort(
                Comparator
                        .comparingLong((Case casa) -> giorniDisponibiliPerCasa.getOrDefault(casa.getId(), 0L))
                        .reversed()
                        .thenComparing(Case::getId)
        );

        return caseList;
    }

    private long calculateAvailableDays(Long casaId, List<Prenotazione> prenotazioni, LocalDate start, LocalDate end) {
        List<LocalDate[]> ranges = new ArrayList<>();
        for (Prenotazione prenotazione : prenotazioni) {
            if (!casaId.equals(prenotazione.getCasaId())) {
                continue;
            }

            LocalDate bookingStart = prenotazione.getCheckIn();
            LocalDate bookingEnd = prenotazione.getCheckOut();

            if (bookingStart == null || bookingEnd == null || !bookingStart.isBefore(bookingEnd)) {
                continue;
            }

            LocalDate clippedStart = bookingStart.isAfter(start) ? bookingStart : start;
            LocalDate clippedEnd = bookingEnd.isBefore(end) ? bookingEnd : end;

            if (clippedStart.isBefore(clippedEnd)) {
                ranges.add(new LocalDate[]{clippedStart, clippedEnd});
            }
        }

        if (ranges.isEmpty()) {
            return ChronoUnit.DAYS.between(start, end);
        }

        ranges.sort(Comparator.comparing(range -> range[0]));

        long occupiedDays = 0;
        LocalDate currentStart = ranges.get(0)[0];
        LocalDate currentEnd = ranges.get(0)[1];

        for (int i = 1; i < ranges.size(); i++) {
            LocalDate nextStart = ranges.get(i)[0];
            LocalDate nextEnd = ranges.get(i)[1];

            if (!nextStart.isAfter(currentEnd)) {
                if (nextEnd.isAfter(currentEnd)) {
                    currentEnd = nextEnd;
                }
            } else {
                occupiedDays += ChronoUnit.DAYS.between(currentStart, currentEnd);
                currentStart = nextStart;
                currentEnd = nextEnd;
            }
        }

        occupiedDays += ChronoUnit.DAYS.between(currentStart, currentEnd);

        long totalDays = ChronoUnit.DAYS.between(start, end);
        long availableDays = totalDays - occupiedDays;
        return Math.max(availableDays, 0);
    }
}
