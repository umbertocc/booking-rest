package com.example.mail.repository;

import com.example.mail.model.PrezzoCasa;
import com.example.mail.model.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrezzoCasaRepository extends JpaRepository<PrezzoCasa, Long> {
    List<PrezzoCasa> findByCasaAndInizioPeriodoLessThanEqualAndFinePeriodoGreaterThanEqual(Case casa, LocalDate checkIn, LocalDate checkOut);
    List<PrezzoCasa> findByCasa(Case casa);
    List<PrezzoCasa> findByCasaId(Long casaId);
}
