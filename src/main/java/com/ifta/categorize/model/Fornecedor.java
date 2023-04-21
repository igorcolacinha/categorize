package com.ifta.categorize.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document
public class Fornecedor {
    @Id
    private String id;
    private String nome;
    private String cnpj;
    private String endereco;

    public Fornecedor(String nome, String cnpj, String endereco) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.endereco = endereco;
    }
}
