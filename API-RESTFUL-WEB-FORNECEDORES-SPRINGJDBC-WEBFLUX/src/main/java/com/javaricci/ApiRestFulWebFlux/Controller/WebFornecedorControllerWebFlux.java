package com.javaricci.ApiRestFulWebFlux.Controller;

import com.javaricci.ApiRestFulWebFlux.Entity.Fornecedor;
import com.javaricci.ApiRestFulWebFlux.Service.FornecedorServiceWebFlux;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

// NOTA: No WebFlux com Thymeleaf Reactive:
// - O @Controller continua igual
// - Os métodos passam a retornar Mono<String> em vez de String
// - O Model continua funcionando normalmente com Thymeleaf Reactive
// - @ModelAttribute recebe Mono<Fornecedor> no WebFlux (binding reativo)
// - Dependência necessária: thymeleaf-extras-springsecurity5 substituída por
//   spring-boot-starter-thymeleaf que já inclui o suporte reativo via
//   thymeleaf-spring5 quando WebFlux está no classpath.

@Controller
@RequestMapping("/fornecedores")
public class WebFornecedorControllerWebFlux {

    @Autowired
    private FornecedorServiceWebFlux serviceWebFlux;

    // GET /fornecedores — lista todos os fornecedores
    @GetMapping
    public Mono<String> listarTodos(Model model) {
        return serviceWebFlux.listarTodos()
                .collectList()
                .doOnNext(lista -> model.addAttribute("fornecedores", lista))
                .thenReturn("fornecedores/listar");
        // collectList() agrupa Flux<Fornecedor> em Mono<List<Fornecedor>>
        // para que o Thymeleaf possa iterar com th:each
    }

    // GET /fornecedores/novo — exibe formulário vazio
    @GetMapping("/novo")
    public Mono<String> novoFornecedor(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return Mono.just("fornecedores/formulario");
    }

    // POST /fornecedores/salvar — salva e redireciona para a lista
    @PostMapping("/salvar")
    public Mono<String> salvar(@ModelAttribute Fornecedor fornecedor) {
        return serviceWebFlux.salvar(fornecedor)
                .thenReturn("redirect:/fornecedores");
        // thenReturn() descarta o resultado e retorna a string de redirect
    }

    // GET /fornecedores/editar/{id} — exibe formulário com dados do fornecedor
    @GetMapping("/editar/{id}")
    public Mono<String> editar(@PathVariable Long id, Model model) {
        return serviceWebFlux.buscarPorId(id)
                .defaultIfEmpty(new Fornecedor())
                .doOnNext(f -> model.addAttribute("fornecedor", f))
                .thenReturn("fornecedores/formulario");
        // defaultIfEmpty substitui o orElse(new Fornecedor()) do MVC
    }

    // GET /fornecedores/deletar/{id} — deleta e redireciona
    @GetMapping("/deletar/{id}")
    public Mono<String> deletar(@PathVariable Long id) {
        return serviceWebFlux.deletar(id)
                .thenReturn("redirect:/fornecedores");
    }
}