package com.example.mail.repository;

import com.example.mail.model.Prenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione, UUID> {
    @Query("SELECT p FROM Prenotazione p WHERE p.casaId = :casaId AND p.checkIn < :dataFine AND p.checkOut > :dataInizio")
    List<Prenotazione> findPrenotazioniConflitto(@Param("casaId") Long casaId, @Param("dataInizio") LocalDate dataInizio, @Param("dataFine") LocalDate dataFine);
    @Query("SELECT p FROM Prenotazione p WHERE p.checkIn < :dataFine AND p.checkOut > :dataInizio")
    List<Prenotazione> findPrenotazioniConflittoPerTutteLeCase(@Param("dataInizio") LocalDate dataInizio, @Param("dataFine") LocalDate dataFine);
}
