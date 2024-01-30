package com.ssafy.backend.dagak.model.repository;

import com.ssafy.backend.dagak.model.domain.Gak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GakRepository extends JpaRepository<Gak, Integer> {



}