package com.example.mail.repository;

import com.example.mail.model.Case;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
	List<Case> findByEmailAndPassword(String email, String password);
	List<Case> findByEmail(String email);
	Optional<Case> findFirstByNomeIgnoreCase(String nome);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT c FROM Case c WHERE c.id = :id")
	Optional<Case> findByIdForUpdate(Long id);
}
