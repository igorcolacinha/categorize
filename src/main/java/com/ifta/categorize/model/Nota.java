package com.ifta.categorize.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@Document
public class Nota {
    @Id
    private String id;

    private String url;

    private String chave;

    private String numero;

    private String serie;

    private ZonedDateTime emissao;

    @DBRef
    private List<Produto> produtos;

    @DBRef
    private Fornecedor fornecedor;

    public Nota(String url) {
        this.url = url;
    }
}