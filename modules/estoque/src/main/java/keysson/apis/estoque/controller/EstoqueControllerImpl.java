package keysson.apis.estoque.controller;

import keysson.apis.estoque.dto.request.*;
import keysson.apis.estoque.dto.response.*;
import keysson.apis.estoque.exception.BusinessRuleException;
import keysson.apis.estoque.service.EstoqueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
public class EstoqueControllerImpl implements EstoqueController {

    private final EstoqueService estoqueService;

    @Autowired
    public EstoqueControllerImpl(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    // ============================================
    // CENTROS DE ARMAZENAMENTO
    // ============================================

    @Override
    public ResponseEntity<List<CentroArmazenamentoResponse>> listarCentros() throws BusinessRuleException {
        log.info("Recebendo requisição para listar centros de armazenamento");
        return ResponseEntity.ok(estoqueService.listarCentros());
    }

    @Override
    public ResponseEntity<CentroArmazenamentoResponse> buscarCentro(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para buscar centro ID: {}", id);
        return ResponseEntity.ok(estoqueService.buscarCentro(id));
    }

    @Override
    public ResponseEntity<Long> cadastrarCentro(CadastrarCentroRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para cadastrar centro: {}", request.nome());
        Long id = estoqueService.cadastrarCentro(request);
        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity<Void> atualizarCentro(Long id, CadastrarCentroRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para atualizar centro ID: {}", id);
        estoqueService.atualizarCentro(id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> desativarCentro(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para desativar centro ID: {}", id);
        estoqueService.desativarCentro(id);
        return ResponseEntity.ok().build();
    }

    // ============================================
    // LOCALIZAÇÕES
    // ============================================

    @Override
    public ResponseEntity<List<LocalizacaoResponse>> listarLocalizacoesPorCentro(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para listar localizações do centro ID: {}", id);
        return ResponseEntity.ok(estoqueService.listarLocalizacoesPorCentro(id));
    }

    @Override
    public ResponseEntity<Long> cadastrarLocalizacao(Long id, CadastrarLocalizacaoRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para cadastrar localização '{}' no centro ID: {}", request.codigo(), id);
        Long idLocalizacao = estoqueService.cadastrarLocalizacao(id, request);
        return ResponseEntity.ok(idLocalizacao);
    }

    // ============================================
    // VÍNCULO PRODUTO ↔ LOCALIZAÇÃO
    // ============================================

    @Override
    public ResponseEntity<Void> vincularProdutoLocalizacao(VincularProdutoLocalizacaoRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para vincular produto {} à localização {}", request.idProduto(), request.idLocalizacao());
        estoqueService.vincularProdutoLocalizacao(request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<ProdutoLocalizacaoResponse>> listarLocalizacoesProduto(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para listar localizações do produto ID: {}", id);
        return ResponseEntity.ok(estoqueService.listarLocalizacoesProduto(id));
    }

    // ============================================
    // PRODUTOS
    // ============================================

    @Override
    public ResponseEntity<List<ProdutoResponse>> listarProdutos() throws BusinessRuleException {
        log.info("Recebendo requisição para listar produtos");
        return ResponseEntity.ok(estoqueService.listarProdutos());
    }

    @Override
    public ResponseEntity<ProdutoResponse> buscarProduto(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para buscar produto ID: {}", id);
        return ResponseEntity.ok(estoqueService.buscarProduto(id));
    }

    @Override
    public ResponseEntity<Long> cadastrarProduto(CadastrarProdutoRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para cadastrar produto: {}", request.nome());
        Long id = estoqueService.cadastrarProduto(request);
        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity<Void> atualizarProduto(Long id, AtualizarProdutoRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para atualizar produto ID: {}", id);
        estoqueService.atualizarProduto(id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> desativarProduto(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para desativar produto ID: {}", id);
        estoqueService.desativarProduto(id);
        return ResponseEntity.ok().build();
    }

    // ============================================
    // CATEGORIAS
    // ============================================

    @Override
    public ResponseEntity<List<CategoriaResponse>> listarCategorias() throws BusinessRuleException {
        log.info("Recebendo requisição para listar categorias");
        return ResponseEntity.ok(estoqueService.listarCategorias());
    }

    // ============================================
    // ESTOQUE (MOVIMENTAÇÕES)
    // ============================================

    @Override
    public ResponseEntity<Long> registrarEntrada(RegistrarEntradaRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para registrar entrada: produto={}, qtd={}", request.idProduto(), request.quantidade());
        Long id = estoqueService.registrarEntrada(request);
        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity<Void> registrarSaida(RegistrarSaidaRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para registrar saída: produto={}, qtd={}", request.idProduto(), request.quantidade());
        estoqueService.registrarSaida(request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<MovimentacaoResponse>> listarMovimentacoes(
            String tipo,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) throws BusinessRuleException {
        log.info("Recebendo requisição para listar movimentações: tipo={}", tipo);
        return ResponseEntity.ok(estoqueService.listarMovimentacoes(tipo, dataInicio, dataFim));
    }

    @Override
    public ResponseEntity<List<MovimentacaoResponse>> historicoProduto(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para histórico de movimentações do produto ID: {}", id);
        return ResponseEntity.ok(estoqueService.historicoProduto(id));
    }

    @Override
    public ResponseEntity<EstoqueDisponivelResponse> estoqueDisponivel(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para estoque disponível do produto ID: {}", id);
        return ResponseEntity.ok(estoqueService.estoqueDisponivel(id));
    }

    // ============================================
    // INVENTÁRIO
    // ============================================

    @Override
    public ResponseEntity<Long> cadastrarInventario(Long idProduto) throws BusinessRuleException {
        log.info("Recebendo requisição para iniciar inventário do produto ID: {}", idProduto);
        Long id = estoqueService.cadastrarInventario(idProduto);
        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity<Void> registrarContagem(Long id, RegistrarContagemRequest request) throws BusinessRuleException {
        log.info("Recebendo requisição para registrar contagem no inventário ID: {}", id);
        estoqueService.registrarContagem(id, request);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<InventarioResponse>> listarDivergencias() throws BusinessRuleException {
        log.info("Recebendo requisição para listar divergências do inventário");
        return ResponseEntity.ok(estoqueService.listarDivergencias());
    }

    @Override
    public ResponseEntity<Void> ajustarInventario(Long id) throws BusinessRuleException {
        log.info("Recebendo requisição para ajustar inventário ID: {}", id);
        estoqueService.ajustarInventario(id);
        return ResponseEntity.ok().build();
    }

    // ============================================
    // DASHBOARD
    // ============================================

    @Override
    public ResponseEntity<DashboardResponse> buscarDashboard() throws BusinessRuleException {
        log.info("Recebendo requisição para dashboard");
        return ResponseEntity.ok(estoqueService.buscarDashboard());
    }

    // ============================================
    // RELATÓRIOS
    // ============================================

    @Override
    public ResponseEntity<List<RelatorioValorResponse>> relatorioValor() throws BusinessRuleException {
        log.info("Recebendo requisição para relatório de valor");
        return ResponseEntity.ok(estoqueService.relatorioValor());
    }

    @Override
    public ResponseEntity<List<RelatorioGiroResponse>> relatorioGiro() throws BusinessRuleException {
        log.info("Recebendo requisição para relatório de giro");
        return ResponseEntity.ok(estoqueService.relatorioGiro());
    }
}
