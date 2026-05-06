package com.javaricci.ApiRestFulWebFlux.Repository;

import com.javaricci.ApiRestFulWebFlux.Entity.Fornecedor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// NOTA: No WebFlux os retornos são tipos reativos:
//   - Flux<T>  equivale a List<T>   (0..N elementos)
//   - Mono<T>  equivale a Optional<T> / objeto único (0..1 elemento)
// O JdbcTemplate é bloqueante; por isso as operações são executadas em
// scheduler Schedulers.boundedElastic() para não bloquear o event loop.

public interface FornecedorRepositoryWebFlux {
    Flux<Fornecedor> findAll();
    Mono<Fornecedor> findById(Long id);
    Mono<Fornecedor> save(Fornecedor fornecedor);
    Mono<Void> deleteById(Long id);
}