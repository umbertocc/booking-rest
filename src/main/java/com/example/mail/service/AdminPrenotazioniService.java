package com.example.mail.service;

import com.example.mail.model.Case;
import com.example.mail.model.Prenotazione;
import com.example.mail.dto.PrenotazioneDTO;
import com.example.mail.repository.CaseRepository;
import com.example.mail.repository.PrenotazioneRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminPrenotazioniService {
    private final PrenotazioneRepository prenotazioneRepository;
    private final CaseRepository caseRepository;

    public AdminPrenotazioniService(PrenotazioneRepository prenotazioneRepository, CaseRepository caseRepository) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.caseRepository = caseRepository;
    }

    public Map<Case, List<Prenotazione>> getPrenotazioniPerCasa() {
        List<Case> caseList = caseRepository.findAll();
        List<Prenotazione> prenotazioni = prenotazioneRepository.findAll();
        Map<Long, Case> caseMap = caseList.stream().collect(Collectors.toMap(Case::getId, c -> c));
        Map<Case, List<Prenotazione>> result = new LinkedHashMap<>();
        for (Case c : caseList) {
            result.put(c, new ArrayList<>());
        }
        for (Prenotazione p : prenotazioni) {
            Case c = caseMap.get(p.getCasaId());
            if (c != null) {
                result.get(c).add(p);
            }
        }
        return result;
    }

        public PrenotazioneDTO updatePrenotazione(java.util.UUID id, PrenotazioneDTO dto) {
            Optional<Prenotazione> opt = prenotazioneRepository.findById(id);
            if (opt.isEmpty()) return null;
            Prenotazione p = opt.get();
            // Aggiorna solo i campi non null del DTO
            if (dto.getOspiteNome() != null) p.setOspiteNome(dto.getOspiteNome());
            if (dto.getCheckIn() != null) p.setCheckIn(dto.getCheckIn());
            if (dto.getCheckOut() != null) p.setCheckOut(dto.getCheckOut());
            if (dto.getEmailOspite() != null) p.setEmailOspite(dto.getEmailOspite());
            if (dto.getTelefonoOspite() != null) p.setTelefonoOspite(dto.getTelefonoOspite());
            if (dto.getPrezzoTotale() != null) p.setPrezzoTotale(dto.getPrezzoTotale());
            if (dto.getCaparra() != null) p.setCaparra(dto.getCaparra());
            if (dto.getNote() != null) p.setNote(dto.getNote());
            // Non aggiorniamo id, casaId, createdAt
            prenotazioneRepository.save(p);
            PrenotazioneDTO out = new PrenotazioneDTO();
            out.setId(p.getId());
            out.setOspiteNome(p.getOspiteNome());
            out.setCheckIn(p.getCheckIn());
            out.setCheckOut(p.getCheckOut());
            out.setEmailOspite(p.getEmailOspite());
            out.setNote(p.getNote());
            out.setTelefonoOspite(p.getTelefonoOspite());
            out.setPrezzoTotale(p.getPrezzoTotale());
            out.setCaparra(p.getCaparra());
            out.setCreatedAt(p.getCreatedAt());
            return out;
        }

     public boolean deletePrenotazione(java.util.UUID id) {
        if (prenotazioneRepository.existsById(id)) {
            prenotazioneRepository.deleteById(id);
            return true;
        }
        return false;
    }

        public PrenotazioneDTO createPrenotazione(com.example.mail.dto.PrenotazioneDTO dto) {
        // Verifica campi obbligatori
        if (dto.getOspiteNome() == null || dto.getCheckIn() == null || dto.getCheckOut() == null || dto.getCasaId() == null) {
            return null;
        }
        Prenotazione p = new Prenotazione();
        p.setOspiteNome(dto.getOspiteNome());
        p.setCheckIn(dto.getCheckIn());
        p.setCheckOut(dto.getCheckOut());
        p.setEmailOspite(dto.getEmailOspite());
        p.setTelefonoOspite(dto.getTelefonoOspite());
        p.setPrezzoTotale(dto.getPrezzoTotale());
        p.setCaparra(dto.getCaparra());
        p.setNote(dto.getNote());
        p.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : java.time.OffsetDateTime.now());
        p.setCasaId(dto.getCasaId());
        prenotazioneRepository.save(p);
        PrenotazioneDTO out = new PrenotazioneDTO();
        out.setId(p.getId());
        out.setOspiteNome(p.getOspiteNome());
        out.setCheckIn(p.getCheckIn());
        out.setCheckOut(p.getCheckOut());
        out.setEmailOspite(p.getEmailOspite());
        out.setTelefonoOspite(p.getTelefonoOspite());
        out.setPrezzoTotale(p.getPrezzoTotale());
        out.setCaparra(p.getCaparra());
        out.setNote(p.getNote());
        out.setCreatedAt(p.getCreatedAt());
        out.setCasaId(p.getCasaId());
        return out;
    }
}
