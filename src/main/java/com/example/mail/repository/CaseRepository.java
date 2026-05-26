package com.example.mail.repository;

import com.example.mail.model.Case;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
	List<Case> findByEmailAndPassword(String email, String password);
	List<Case> findByEmail(String email);
}
