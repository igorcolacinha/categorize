package com.ifta.categorize.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document
public class Produto {
    @Id
    private String id;
    private String nome;
    private String codigo;
    private String quantidade;
    private String unidade;
    private String valorUnitario;
    private String total;
}
