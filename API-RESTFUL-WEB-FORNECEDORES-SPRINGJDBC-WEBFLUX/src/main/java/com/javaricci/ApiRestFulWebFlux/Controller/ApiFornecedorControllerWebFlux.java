package com.javaricci.ApiRestFulWebFlux.Controller;

import com.javaricci.ApiRestFulWebFlux.Entity.Fornecedor;
import com.javaricci.ApiRestFulWebFlux.Service.FornecedorServiceWebFlux;
import com.javaricci.ApiRestFulWebFlux.Service.ReportServiceWebFlux;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// NOTA: No WebFlux o @RestController continua igual.
// As principais mudanças são nos tipos de retorno dos métodos:
//   - List<T>      →  Flux<T>
//   - Optional<T>  →  Mono<T>
//   - void         →  Mono<Void>
//   - T            →  Mono<T>
// O Spring WebFlux subscreve automaticamente os publishers e envia a resposta HTTP.

@RestController
@RequestMapping("/api/fornecedores")
public class ApiFornecedorControllerWebFlux {

    @Autowired
    private FornecedorServiceWebFlux serviceWebFlux;

    @Autowired
    private ReportServiceWebFlux reportServiceWebFlux;

    // GET /api/fornecedores — retorna todos os fornecedores como stream reativo
    @GetMapping
    public Flux<Fornecedor> listarTodos() {
        return serviceWebFlux.listarTodos();
    }

    // GET /api/fornecedores/{id} — retorna um fornecedor ou 404
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Fornecedor>> buscarPorId(@PathVariable Long id) {
        return serviceWebFlux.buscarPorId(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
        // defaultIfEmpty substitui o Optional.empty() do MVC
    }

    // POST /api/fornecedores — cria novo fornecedor
    @PostMapping
    public Mono<Fornecedor> salvar(@RequestBody Fornecedor fornecedor) {
        return serviceWebFlux.salvar(fornecedor);
    }

    // PUT /api/fornecedores/{id} — atualiza fornecedor existente
    @PutMapping("/{id}")
    public Mono<Fornecedor> atualizar(@PathVariable Long id, @RequestBody Fornecedor fornecedor) {
        fornecedor.setId(id);
        return serviceWebFlux.salvar(fornecedor);
    }

    // DELETE /api/fornecedores/{id} — remove fornecedor
    @DeleteMapping("/{id}")
    public Mono<Void> deletar(@PathVariable Long id) {
        return serviceWebFlux.deletar(id);
        // Mono<Void> = resposta 200 vazia no WebFlux
    }

    // GET /api/fornecedores/relatorio — gera e retorna PDF
    @GetMapping("/relatorio")
    public Mono<ResponseEntity<byte[]>> gerarRelatorio() {
        return reportServiceWebFlux.gerarRelatorioFornecedoresWebFlux()
                .map(pdf -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=fornecedores.pdf")
                        .body(pdf));
    }
}