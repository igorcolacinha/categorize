package com.ifta.categorize.controller;

import com.ifta.categorize.service.NotaFiscalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/notas")
public class NotaController {
    private final NotaFiscalService notaFiscalService;

    public NotaController(NotaFiscalService notaFiscalService) {
        this.notaFiscalService = notaFiscalService;
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        return ResponseEntity.ok(notaFiscalService.listar());
    }

    @PostMapping
    public ResponseEntity<?> cadastrarNota(@RequestHeader(name = "urlQrCode") String urlQrCode, UriComponentsBuilder uriBuilder) {
        return notaFiscalService.cadastrar(urlQrCode, uriBuilder);
    }

    @PutMapping("/reprocessar")
    public ResponseEntity<?> reprocessarNota(@RequestHeader(name = "urlQrCode") String urlQrCode, UriComponentsBuilder uriBuilder) {
        return notaFiscalService.reprocessar(urlQrCode, uriBuilder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> consultarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(notaFiscalService.consultarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }

    @GetMapping("/{chave}")
    public ResponseEntity<?> consultarPorChave(@PathVariable String chave) {
        try {
            return ResponseEntity.ok(notaFiscalService.consultarPorChave(chave));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e);
        }
    }
}
