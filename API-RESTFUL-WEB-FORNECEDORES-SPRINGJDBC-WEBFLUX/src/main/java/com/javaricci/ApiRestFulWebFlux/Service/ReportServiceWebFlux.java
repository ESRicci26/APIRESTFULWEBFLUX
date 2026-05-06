package com.javaricci.ApiRestFulWebFlux.Service;

import com.javaricci.ApiRestFulWebFlux.Entity.Fornecedor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// NOTA: A geração de relatório com JasperReports é totalmente bloqueante
// (compilação + preenchimento + exportação em memória).
// Portanto o método é envolto em Mono.fromCallable(...)
// .subscribeOn(Schedulers.boundedElastic()) para não bloquear o event loop.

@Service
public class ReportServiceWebFlux {

    private final FornecedorServiceWebFlux fornecedorServiceWebFlux;

    public ReportServiceWebFlux(FornecedorServiceWebFlux fornecedorServiceWebFlux) {
        this.fornecedorServiceWebFlux = fornecedorServiceWebFlux;
    }

    public Mono<byte[]> gerarRelatorioFornecedoresWebFlux() {
        // 1. Buscar todos os fornecedores de forma reativa e depois gerar o relatório
        return fornecedorServiceWebFlux.listarTodos()
                .collectList()  // converte Flux<Fornecedor> → Mono<List<Fornecedor>>
                .flatMap(lista -> Mono.fromCallable(() -> gerarPdf(lista))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    // Método privado bloqueante — executado fora do event loop
    private byte[] gerarPdf(List<Fornecedor> lista) throws Exception {

        // 2. Carregar e compilar o jrxml
        JasperReport jasperReport = JasperCompileManager.compileReport(
            getClass().getResourceAsStream("/reports/Fornecedores.jrxml")
        );

        // 3. Converter para DataSource do Jasper
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(lista);

        // 4. Parâmetros
        Map<String, Object> params = new HashMap<>();
        params.put("autor", "Sistema Spring WebFlux");

        // 5. Preencher o relatório
        JasperPrint print = JasperFillManager.fillReport(jasperReport, params, dataSource);

        // 6. Exportar para PDF em bytes
        return JasperExportManager.exportReportToPdf(print);
    }
}