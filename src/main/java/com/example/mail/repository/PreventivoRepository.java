package com.example.mail.repository;

import com.example.mail.model.Preventivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PreventivoRepository extends JpaRepository<Preventivo, UUID> {
}
