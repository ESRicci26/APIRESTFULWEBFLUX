# 🌿 API RESTful WebFlux — Cadastro de Fornecedores

![Java](https://img.shields.io/badge/Java-11-007396?style=flat-square&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.6.1-6DB33F?style=flat-square&logo=springboot)
![Spring WebFlux](https://img.shields.io/badge/Spring%20WebFlux-Reativo-6DB33F?style=flat-square&logo=spring)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite)
![JasperReports](https://img.shields.io/badge/JasperReports-6.21.3-orange?style=flat-square)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203-85EA2D?style=flat-square&logo=swagger)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

---

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [O que é Spring WebFlux?](#-o-que-é-spring-webflux)
- [Diferenças entre Spring MVC e Spring WebFlux](#-diferenças-entre-spring-mvc-e-spring-webflux)
- [Arquitetura Reativa — Mono e Flux](#-arquitetura-reativa--mono-e-flux)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Diagrama de Arquitetura](#-diagrama-de-arquitetura)
- [Endpoints da API](#-endpoints-da-api)
- [Endpoints Web (Thymeleaf)](#-endpoints-web-thymeleaf)
- [Como Executar](#-como-executar)
- [Banco de Dados SQLite](#-banco-de-dados-sqlite)
- [Relatório PDF com JasperReports](#-relatório-pdf-com-jasperreports)
- [Swagger UI](#-swagger-ui)
- [JdbcTemplate + WebFlux — Integração com Código Bloqueante](#-jdbctemplate--webflux--integração-com-código-bloqueante)
- [Considerações sobre a Migração MVC → WebFlux](#-considerações-sobre-a-migração-mvc--webflux)

---

## 📌 Sobre o Projeto

Este projeto é a **versão reativa** de uma aplicação Spring Boot de cadastro de fornecedores. Ele foi migrado do modelo tradicional **Spring MVC** (bloqueante/síncrono) para o modelo **Spring WebFlux** (não-bloqueante/assíncrono), mantendo todas as funcionalidades originais:

- ✅ API RESTful completa (CRUD de Fornecedores)
- ✅ Interface Web com Thymeleaf
- ✅ Banco de dados SQLite via JdbcTemplate
- ✅ Geração de relatório PDF com JasperReports
- ✅ Documentação Swagger / OpenAPI 3

A aplicação serve como estudo comparativo e demonstração prática de como adaptar um projeto Spring convencional ao paradigma reativo, com explicações detalhadas de cada decisão de conversão.

---

## 🔄 O que é Spring WebFlux?

**Spring WebFlux** é o módulo reativo do Spring Framework, introduzido na versão 5.0. Ele foi criado para atender à necessidade de aplicações que precisam lidar com **alta concorrência** com **baixo consumo de recursos**, sem a necessidade de alocar uma thread por requisição — modelo esse característico do Spring MVC.

### Modelo Imperativo vs. Modelo Reativo

| Característica | Spring MVC (Imperativo) | Spring WebFlux (Reativo) |
|---|---|---|
| Modelo de execução | Bloqueante / Síncrono | Não-bloqueante / Assíncrono |
| Servidor padrão | Tomcat (Servlet) | Netty (Event Loop) |
| Threads por requisição | 1 thread por requisição | Poucas threads, event loop compartilhado |
| Escalabilidade | Limitada pelo pool de threads | Alta — suporta milhares de conexões simultâneas |
| Retorno dos métodos | `String`, `List<T>`, `Optional<T>` | `Mono<T>`, `Flux<T>` |
| Curva de aprendizado | Baixa | Média/Alta |
| Ideal para | Aplicações CRUD tradicionais | APIs de alta demanda, streaming, microserviços |

### Por que usar WebFlux?

O WebFlux se destaca em cenários onde:

- A aplicação precisa sustentar **muitas conexões simultâneas** com latência baixa
- Existem **chamadas a serviços externos** (APIs REST, banco de dados reativo, mensageria) que introduzem tempo de espera
- O sistema trabalha com **streaming de dados** contínuo (ex: SSE — Server-Sent Events)
- O ambiente é de **microserviços**, onde a comunicação entre serviços é frequente

### Como o WebFlux funciona por baixo dos panos?

O WebFlux é construído sobre o **Project Reactor**, que implementa a especificação **Reactive Streams**. O modelo de execução segue o padrão **Event Loop** (similar ao Node.js), onde um número reduzido de threads fica responsável por processar eventos de I/O de forma não-bloqueante.

```
Requisição HTTP
      │
      ▼
  Netty (Event Loop)
      │
      ▼
  Router / Controller  →  retorna Mono<T> ou Flux<T>
      │
      ▼
  Pipeline Reativo (operadores: map, flatMap, filter...)
      │
      ▼
  Subscriber (Spring serializa e devolve a resposta HTTP)
```

O segredo está na **subscrição lazy**: nenhum código reativo é executado até que haja um `subscribe()`. O Spring WebFlux faz essa subscrição automaticamente ao receber uma requisição HTTP.

---

## ⚖️ Diferenças entre Spring MVC e Spring WebFlux

A seguir, uma comparação direta do código original (MVC) com o código convertido (WebFlux) deste projeto.

### 1. Dependência principal — `pom.xml`

```xml
<!-- Spring MVC (original) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring WebFlux (convertido) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

> Trocar `spring-boot-starter-web` por `spring-boot-starter-webflux` faz o Spring Boot substituir o **Tomcat** pelo **Netty** como servidor HTTP automaticamente.

---

### 2. Configuração CORS — `WebConfig`

```java
// Spring MVC
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) { ... }
}

// Spring WebFlux
@Configuration
public class WebConfigWebFlux implements WebFluxConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) { ... }
}
```

> `WebMvcConfigurer` → `WebFluxConfigurer`  
> `@EnableWebMvc` → `@EnableWebFlux` (omitido em Spring Boot para não conflitar com autoconfiguração)

---

### 3. Interface do Repositório

```java
// Spring MVC
public interface FornecedorRepository {
    List<Fornecedor> findAll();
    Optional<Fornecedor> findById(Long id);
    Fornecedor save(Fornecedor fornecedor);
    void deleteById(Long id);
}

// Spring WebFlux
public interface FornecedorRepositoryWebFlux {
    Flux<Fornecedor> findAll();
    Mono<Fornecedor> findById(Long id);
    Mono<Fornecedor> save(Fornecedor fornecedor);
    Mono<Void> deleteById(Long id);
}
```

> `List<T>` → `Flux<T>` | `Optional<T>` → `Mono<T>` | `void` → `Mono<Void>`

---

### 4. Service

```java
// Spring MVC
public List<Fornecedor> listarTodos() {
    return repository.findAll();
}

// Spring WebFlux
public Flux<Fornecedor> listarTodos() {
    return repository.findAll();
}
```

---

### 5. REST Controller

```java
// Spring MVC
@GetMapping
public List<Fornecedor> listarTodos() {
    return service.listarTodos();
}

@GetMapping("/{id}")
public Optional<Fornecedor> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id);
}

@DeleteMapping("/{id}")
public void deletar(@PathVariable Long id) {
    service.deletar(id);
}

// Spring WebFlux
@GetMapping
public Flux<Fornecedor> listarTodos() {
    return serviceWebFlux.listarTodos();
}

@GetMapping("/{id}")
public Mono<ResponseEntity<Fornecedor>> buscarPorId(@PathVariable Long id) {
    return serviceWebFlux.buscarPorId(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
}

@DeleteMapping("/{id}")
public Mono<Void> deletar(@PathVariable Long id) {
    return serviceWebFlux.deletar(id);
}
```

---

### 6. Web Controller (Thymeleaf)

```java
// Spring MVC
@GetMapping
public String listarTodos(Model model) {
    model.addAttribute("fornecedores", service.listarTodos());
    return "fornecedores/listar";
}

// Spring WebFlux
@GetMapping
public Mono<String> listarTodos(Model model) {
    return serviceWebFlux.listarTodos()
            .collectList()
            .doOnNext(lista -> model.addAttribute("fornecedores", lista))
            .thenReturn("fornecedores/listar");
}
```

> `collectList()` converte `Flux<Fornecedor>` em `Mono<List<Fornecedor>>` para que o Thymeleaf consiga iterar com `th:each`.

---

### 7. Swagger — dependência

```xml
<!-- Spring MVC -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.6.15</version>
</dependency>

<!-- Spring WebFlux -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-webflux-ui</artifactId>
    <version>1.6.15</version>
</dependency>
```

---

## 🧩 Arquitetura Reativa — Mono e Flux

O **Project Reactor** fornece dois tipos publishers que representam sequências assíncronas:

### `Mono<T>` — 0 ou 1 elemento

```
Mono<Fornecedor>
  │
  ├── onNext(fornecedor)   → emite 1 valor
  ├── onComplete()         → finaliza sem valor (Mono.empty())
  └── onError(ex)          → erro
```

Usado quando o resultado pode ser um único objeto ou vazio — equivalente ao `Optional<T>` no modelo síncrono.

### `Flux<T>` — 0 a N elementos

```
Flux<Fornecedor>
  │
  ├── onNext(f1)
  ├── onNext(f2)
  ├── onNext(f3)
  ├── onComplete()
  └── onError(ex)
```

Usado para coleções — equivalente ao `List<T>` no modelo síncrono.

### Operadores mais usados neste projeto

| Operador | Descrição | Equivalente MVC |
|---|---|---|
| `.map()` | Transforma o valor emitido | `stream().map()` |
| `.flatMap()` | Transforma e achata um publisher aninhado | `stream().flatMap()` |
| `.collectList()` | Converte `Flux<T>` → `Mono<List<T>>` | — |
| `.defaultIfEmpty()` | Valor padrão se vazio | `Optional.orElse()` |
| `.thenReturn()` | Descarta o resultado e retorna outro valor | — |
| `.doOnNext()` | Efeito colateral sem alterar o fluxo | — |
| `.then()` | Aguarda conclusão e retorna `Mono<Void>` | — |
| `Mono.fromCallable()` | Envolve código síncrono/bloqueante em Mono | — |
| `.subscribeOn(Schedulers.boundedElastic())` | Executa em thread dedicada (código bloqueante) | — |

---

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Versão | Função |
|---|---|---|
| Java | 11 | Linguagem principal |
| Spring Boot | 2.6.1 | Framework base |
| Spring WebFlux | 5.3.x | Stack reativa HTTP |
| Project Reactor | 3.4.x | Biblioteca Mono/Flux |
| Netty | embutido | Servidor HTTP reativo |
| Spring JDBC | 5.3.x | Acesso ao banco de dados |
| SQLite | 3.x | Banco de dados embarcado |
| Thymeleaf | 3.x | Template engine (Web) |
| JasperReports | 6.21.3 | Geração de relatório PDF |
| SpringDoc OpenAPI | 1.6.15 (WebFlux) | Swagger UI |

---

## 📁 Estrutura do Projeto

```
ApiRestFulWebFlux/
│
├── pom.xml
│
└── src/
    └── main/
        ├── java/
        │   └── com/javaricci/ApiRestFulWebFlux/
        │       │
        │       ├── ApiRestFulWebFluxSpringBootApplication.java   ← Classe principal
        │       │
        │       ├── Config/
        │       │   ├── OpenAPIConfigWebFlux.java                 ← Configuração Swagger
        │       │   └── WebConfigWebFlux.java                     ← Configuração CORS
        │       │
        │       ├── Controller/
        │       │   ├── ApiFornecedorControllerWebFlux.java       ← REST API (Mono/Flux)
        │       │   └── WebFornecedorControllerWebFlux.java       ← Web Thymeleaf (Mono)
        │       │
        │       ├── Entity/
        │       │   └── Fornecedor.java                           ← Entidade de domínio
        │       │
        │       ├── Repository/
        │       │   ├── FornecedorRepositoryWebFlux.java          ← Interface reativa
        │       │   └── FornecedorRepositoryJdbcWebFlux.java      ← Impl. JDBC + Reactor
        │       │
        │       └── Service/
        │           ├── FornecedorServiceWebFlux.java             ← Serviço reativo
        │           └── ReportServiceWebFlux.java                 ← Geração de PDF reativa
        │
        └── resources/
            ├── application.properties
            ├── schema.sql                                         ← DDL do banco SQLite
            ├── reports/
            │   └── Fornecedores.jrxml                            ← Template do relatório
            └── templates/
                └── fornecedores/
                    ├── formulario.html                            ← Formulário Thymeleaf
                    └── listar.html                               ← Listagem Thymeleaf
```

---

## 🗺️ Diagrama de Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Cliente HTTP / Browser                    │
└──────────────────────────┬──────────────────────────────────┘
                           │  HTTP Request
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Netty (Servidor HTTP Reativo)                   │
│                     Event Loop                              │
└──────────────────────────┬──────────────────────────────────┘
                           │
          ┌────────────────┴─────────────────┐
          ▼                                  ▼
┌──────────────────┐              ┌─────────────────────┐
│  ApiFornecedor   │              │  WebFornecedor      │
│  Controller      │              │  Controller         │
│  WebFlux         │              │  WebFlux            │
│  /api/fornecedor │              │  /fornecedores      │
│  → Flux / Mono   │              │  → Mono<String>     │
└────────┬─────────┘              └──────────┬──────────┘
         │                                   │
         └─────────────┬─────────────────────┘
                       ▼
          ┌────────────────────────┐
          │  FornecedorService     │
          │  WebFlux               │
          │  Flux<T> / Mono<T>     │
          └────────────┬───────────┘
                       │
          ┌────────────┴───────────┐
          ▼                        ▼
┌──────────────────┐    ┌──────────────────────┐
│  Fornecedor      │    │  ReportService       │
│  Repository      │    │  WebFlux             │
│  JdbcWebFlux     │    │  Mono<byte[]>        │
│                  │    │  (JasperReports)     │
│  Mono.from       │    └──────────────────────┘
│  Callable() +   │
│  Schedulers      │
│  .bounded        │
│  Elastic()       │
└────────┬─────────┘
         │  Thread pool bloqueante
         ▼
┌─────────────────────┐
│   JdbcTemplate      │
│   (Síncrono/JDBC)   │
└────────┬────────────┘
         │
         ▼
┌─────────────────────┐
│   SQLite            │
│   fornecedores.DB   │
└─────────────────────┘
```

---

## 📡 Endpoints da API

Base URL: `http://localhost:8080/api/fornecedores`

| Método | Endpoint | Descrição | Retorno |
|---|---|---|---|
| `GET` | `/api/fornecedores` | Lista todos os fornecedores | `Flux<Fornecedor>` (JSON Array) |
| `GET` | `/api/fornecedores/{id}` | Busca fornecedor por ID | `Mono<Fornecedor>` ou 404 |
| `POST` | `/api/fornecedores` | Cria novo fornecedor | `Mono<Fornecedor>` |
| `PUT` | `/api/fornecedores/{id}` | Atualiza fornecedor existente | `Mono<Fornecedor>` |
| `DELETE` | `/api/fornecedores/{id}` | Remove fornecedor | `Mono<Void>` (200 vazio) |
| `GET` | `/api/fornecedores/relatorio` | Gera relatório PDF | `Mono<byte[]>` (PDF inline) |

### Exemplo de corpo JSON (POST / PUT)

```json
{
  "nome": "Empresa Exemplo LTDA",
  "cnpj": "12.345.678/0001-99",
  "endereco": "Rua das Flores, 100",
  "bairro": "Centro",
  "municipio": "São Paulo",
  "cep": "01001-000"
}
```

---

## 🌐 Endpoints Web (Thymeleaf)

Base URL: `http://localhost:8080/fornecedores`

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/fornecedores` | Lista todos os fornecedores (HTML) |
| `GET` | `/fornecedores/novo` | Exibe formulário de cadastro |
| `POST` | `/fornecedores/salvar` | Salva fornecedor e redireciona |
| `GET` | `/fornecedores/editar/{id}` | Exibe formulário de edição |
| `GET` | `/fornecedores/deletar/{id}` | Remove e redireciona |

---

## ▶️ Como Executar

### Pré-requisitos

- Java 11 ou superior
- Maven 3.6+

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/ESRicci26/APIRESTFULWEBFLUX.git
cd ApiRestFulWebFlux

# 2. Compile e execute
mvn spring-boot:run

# 3. Acesse a aplicação
# Web:     http://localhost:8080/fornecedores
# API:     http://localhost:8080/api/fornecedores
# Swagger: http://localhost:8080/swagger-ui/index.html
# PDF:     http://localhost:8080/api/fornecedores/relatorio
```

> O banco de dados SQLite (`fornecedores.DB`) é criado automaticamente na raiz do projeto na primeira execução, com base no arquivo `schema.sql`.

---

## 🗄️ Banco de Dados SQLite

O projeto utiliza **SQLite** como banco de dados embarcado, acessado via **JdbcTemplate**. O arquivo `schema.sql` é executado automaticamente na inicialização:

```sql
CREATE TABLE IF NOT EXISTS FORNECEDORES (
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    RAZAOSOCIALFORNECEDOR TEXT,
    CNPJFORNECEDOR        TEXT,
    ENDERECO              TEXT,
    BAIRRO                TEXT,
    MUNICIPIO             TEXT,
    CEP                   TEXT
);
```

Configuração em `application.properties`:

```properties
spring.datasource.url=jdbc:sqlite:fornecedores.DB
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

---

## 📄 Relatório PDF com JasperReports

O relatório é gerado pelo `ReportServiceWebFlux` de forma reativa. Como a biblioteca JasperReports é **totalmente bloqueante** (operações de I/O, compilação e exportação em memória), toda a geração é executada fora do event loop do Reactor, usando `Schedulers.boundedElastic()`:

```java
public Mono<byte[]> gerarRelatorioFornecedoresWebFlux() {
    return fornecedorServiceWebFlux.listarTodos()
            .collectList()
            .flatMap(lista -> Mono.fromCallable(() -> gerarPdf(lista))
                    .subscribeOn(Schedulers.boundedElastic()));
}
```

O template do relatório está em `src/main/resources/reports/Fornecedores.jrxml` e foi criado com o **Jaspersoft Studio Community Edition 7.0.3**.

---

## 📖 Swagger UI

A documentação interativa da API está disponível em:

```
http://localhost:8080/swagger-ui.html
```

A dependência utilizada para WebFlux é a versão específica do SpringDoc:

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-webflux-ui</artifactId>
    <version>1.6.15</version>
</dependency>
```

> ⚠️ A dependência `springdoc-openapi-ui` (sem o `-webflux-`) é destinada ao Spring MVC e **não funciona** com WebFlux.

---

## ⚠️ JdbcTemplate + WebFlux — Integração com Código Bloqueante

Um dos maiores desafios ao migrar para WebFlux é que o **JDBC é inerentemente bloqueante**. Cada chamada ao banco de dados ocupa uma thread enquanto aguarda resposta do banco, o que vai contra o princípio do event loop não-bloqueante.

### O problema

Se uma chamada bloqueante for executada diretamente no event loop do Netty, ela **paralisa todas as outras requisições** processadas por aquela thread, causando degradação severa de performance.

### A solução aplicada neste projeto

Cada operação JDBC é envolto em `Mono.fromCallable()` e executado no scheduler `boundedElastic`, que mantém um pool de threads elástico dedicado a tarefas bloqueantes:

```java
@Override
public Flux<Fornecedor> findAll() {
    return Mono.fromCallable(() -> jdbcTemplate.query(sql, rowMapper))
               .subscribeOn(Schedulers.boundedElastic())
               .flatMapMany(Flux::fromIterable);
}
```

```
Event Loop (Netty)           boundedElastic pool
      │                             │
      │── Mono.fromCallable() ──►   │  executa jdbcTemplate.query()
      │                             │  (bloqueante — ok neste pool)
      │◄── resultado ───────────────│
      │
      │  continua processando
      │  outras requisições
      │  enquanto aguarda
```

### Alternativa totalmente reativa

A solução ideal para produção seria utilizar **Spring Data R2DBC** com um driver reativo. Para SQLite, o suporte ao R2DBC ainda é limitado, por isso este projeto mantém o JdbcTemplate com o padrão `boundedElastic` como alternativa estável e funcional.

---

## 🔁 Considerações sobre a Migração MVC → WebFlux

| Aspecto | Observação |
|---|---|
| **Compatibilidade de anotações** | `@RestController`, `@GetMapping`, `@PostMapping`, `@RequestBody`, `@PathVariable` funcionam igualmente no WebFlux |
| **Thymeleaf** | Compatível com WebFlux sem alteração nos templates; o controller deve usar `collectList()` antes de passar dados ao `Model` |
| **JasperReports** | Biblioteca bloqueante; use `Mono.fromCallable()` + `Schedulers.boundedElastic()` |
| **JdbcTemplate** | Bloqueante por natureza; mesma estratégia do JasperReports |
| **Testes** | Use `StepVerifier` do `reactor-test` para testar `Mono` e `Flux` |
| **`@EnableWebFlux`** | Não usar em projetos Spring Boot — conflita com a autoconfiguração |
| **Swagger** | Trocar `springdoc-openapi-ui` por `springdoc-openapi-webflux-ui` |
| **`Optional<T>`** | Substituído por `Mono<T>` com `.defaultIfEmpty()` |
| **Encoding UTF-8** | Adicionar `<meta charset="UTF-8"/>` nos templates e configurar `spring.thymeleaf.encoding=UTF-8` |

---

## 👨‍💻 Autor

Desenvolvido por **👨‍💻 Edilson Salvador Ricci**

---

## 📜 Licença

Este projeto está sob a licença MIT. Consulte o arquivo [LICENSE](LICENSE) para mais detalhes.
