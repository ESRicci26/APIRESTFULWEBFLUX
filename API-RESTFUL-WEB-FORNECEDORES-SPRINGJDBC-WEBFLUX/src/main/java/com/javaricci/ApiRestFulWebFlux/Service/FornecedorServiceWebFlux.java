package com.javaricci.ApiRestFulWebFlux.Service;

import com.javaricci.ApiRestFulWebFlux.Entity.Fornecedor;
import com.javaricci.ApiRestFulWebFlux.Repository.FornecedorRepositoryWebFlux;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// NOTA: Os métodos agora retornam Flux<T> e Mono<T> em vez de List<T> e Optional<T>.
// Isso garante que o pipeline reativo seja propagado do repositório até o controller.

@Service
public class FornecedorServiceWebFlux {

    @Autowired
    private FornecedorRepositoryWebFlux repository;

    public Flux<Fornecedor> listarTodos() {
        return repository.findAll();
    }

    public Mono<Fornecedor> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Mono<Fornecedor> salvar(Fornecedor fornecedor) {
        return repository.save(fornecedor);
    }

    public Mono<Void> deletar(Long id) {
        return repository.deleteById(id);
    }
}