package com.ifta.categorize.repository;

import com.ifta.categorize.model.Nota;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NotaRepository extends MongoRepository<Nota, String> {
    Optional<Nota> findByUrlIgnoreCase(String url);

    Optional<Nota> findByChaveIgnoreCase(String chave);
}
