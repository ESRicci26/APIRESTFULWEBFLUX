package com.javaricci.ApiRestFulWebFlux.Repository;

import com.javaricci.ApiRestFulWebFlux.Entity.Fornecedor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

// NOTA IMPORTANTE SOBRE WebFlux + JDBC:
// O JdbcTemplate é uma API bloqueante (JDBC síncrono).
// No WebFlux cada operação bloqueante DEVE ser executada em
// Schedulers.boundedElastic() para não bloquear o event loop do Reactor.
// A alternativa totalmente reativa seria usar Spring Data R2DBC, porém o
// driver R2DBC para SQLite tem suporte limitado. Esta implementação mantém
// o JdbcTemplate original envolvendo cada chamada em Mono.fromCallable(...)
// .subscribeOn(Schedulers.boundedElastic()), que é a forma correta de
// integrar código bloqueante com o modelo reativo.

@Repository
public class FornecedorRepositoryJdbcWebFlux implements FornecedorRepositoryWebFlux {

    private final JdbcTemplate jdbcTemplate;

    public FornecedorRepositoryJdbcWebFlux(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ============================================================
    // Mapeamento de ResultSet -> Objeto Fornecedor
    // ============================================================
    private final RowMapper<Fornecedor> rowMapper = new RowMapper<Fornecedor>() {
        @Override
        public Fornecedor mapRow(ResultSet rs, int rowNum) throws SQLException {
            Fornecedor f = new Fornecedor();
            f.setId(rs.getLong("id"));
            f.setNome(rs.getString("RAZAOSOCIALFORNECEDOR"));
            f.setCnpj(rs.getString("CNPJFORNECEDOR"));
            f.setEndereco(rs.getString("ENDERECO"));
            f.setBairro(rs.getString("BAIRRO"));
            f.setMunicipio(rs.getString("MUNICIPIO"));
            f.setCep(rs.getString("CEP"));
            return f;
        }
    };

    // ============================================================
    // MÉTODO: INSERIR NOVO FORNECEDOR
    // ============================================================
    private Fornecedor inserir(Fornecedor fornecedor) {
        String sql =
            "INSERT INTO FORNECEDORES " +
            "(RAZAOSOCIALFORNECEDOR, CNPJFORNECEDOR, ENDERECO, BAIRRO, MUNICIPIO, CEP) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fornecedor.getNome());
            ps.setString(2, fornecedor.getCnpj());
            ps.setString(3, fornecedor.getEndereco());
            ps.setString(4, fornecedor.getBairro());
            ps.setString(5, fornecedor.getMunicipio());
            ps.setString(6, fornecedor.getCep());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            fornecedor.setId(key.longValue());
        }

        return fornecedor;
    }

    // ============================================================
    // MÉTODO: ATUALIZAR FORNECEDOR EXISTENTE
    // ============================================================
    private int atualizar(Fornecedor fornecedor) {
        String sql =
            "UPDATE FORNECEDORES SET " +
            "RAZAOSOCIALFORNECEDOR = ?, " +
            "CNPJFORNECEDOR = ?, " +
            "ENDERECO = ?, " +
            "BAIRRO = ?, " +
            "MUNICIPIO = ?, " +
            "CEP = ? " +
            "WHERE id = ?";

        return jdbcTemplate.update(sql,
                fornecedor.getNome(),
                fornecedor.getCnpj(),
                fornecedor.getEndereco(),
                fornecedor.getBairro(),
                fornecedor.getMunicipio(),
                fornecedor.getCep(),
                fornecedor.getId());
    }

    // ============================================================
    // MÉTODO: EXCLUIR FORNECEDOR PELO ID
    // ============================================================
    private int excluir(Long id) {
        String sql = "DELETE FROM FORNECEDORES WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // ============================================================
    // MÉTODO: LISTAR TODOS OS FORNECEDORES - retorna Flux<Fornecedor>
    // ============================================================
    @Override
    public Flux<Fornecedor> findAll() {
        return Mono.fromCallable(() -> {
            String sql =
                "SELECT id, RAZAOSOCIALFORNECEDOR, CNPJFORNECEDOR, ENDERECO, " +
                "BAIRRO, MUNICIPIO, CEP FROM FORNECEDORES";
            return jdbcTemplate.query(sql, rowMapper);
        })
        .subscribeOn(Schedulers.boundedElastic())  // executa JDBC em thread bloqueante
        .flatMapMany(Flux::fromIterable);
    }

    // ============================================================
    // MÉTODO: BUSCAR FORNECEDOR POR ID - retorna Mono<Fornecedor>
    // ============================================================
    @Override
    public Mono<Fornecedor> findById(Long id) {
        return Mono.fromCallable(() -> {
            String sql =
                "SELECT id, RAZAOSOCIALFORNECEDOR, CNPJFORNECEDOR, ENDERECO, " +
                "BAIRRO, MUNICIPIO, CEP FROM FORNECEDORES WHERE id = ?";
            List<Fornecedor> lista = jdbcTemplate.query(sql, new Object[]{id}, rowMapper);
            return lista.isEmpty() ? Optional.<Fornecedor>empty() : Optional.of(lista.get(0));
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(opt -> opt.map(Mono::just).orElse(Mono.empty()));
    }

    // ============================================================
    // MÉTODO: SALVAR (INSERT ou UPDATE) - retorna Mono<Fornecedor>
    // ============================================================
    @Override
    public Mono<Fornecedor> save(Fornecedor fornecedor) {
        return Mono.fromCallable(() -> {
            if (fornecedor.getId() == null) {
                return inserir(fornecedor);
            } else {
                atualizar(fornecedor);
                return fornecedor;
            }
        })
        .subscribeOn(Schedulers.boundedElastic());
    }

    // ============================================================
    // MÉTODO: DELETAR POR ID - retorna Mono<Void>
    // ============================================================
    @Override
    public Mono<Void> deleteById(Long id) {
        return Mono.fromRunnable(() -> excluir(id))
                   .subscribeOn(Schedulers.boundedElastic())
                   .then();
    }
}