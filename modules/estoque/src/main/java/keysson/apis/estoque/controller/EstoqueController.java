package keysson.apis.estoque.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import keysson.apis.estoque.dto.request.*;
import keysson.apis.estoque.dto.response.*;
import keysson.apis.estoque.exception.BusinessRuleException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequestMapping("/estoque")
@Tag(name = "Estoque", description = "API de gestão de estoque")
public interface EstoqueController {

    // ============================================
    // CENTROS DE ARMAZENAMENTO
    // ============================================

    @GetMapping("/centros")
    @Operation(summary = "Listar centros de armazenamento", description = "Lista todos os centros ativos")
    ResponseEntity<List<CentroArmazenamentoResponse>> listarCentros() throws BusinessRuleException;

    @GetMapping("/centros/{id}")
    @Operation(summary = "Buscar centro por ID", description = "Busca um centro de armazenamento pelo ID")
    ResponseEntity<CentroArmazenamentoResponse> buscarCentro(@PathVariable Long id) throws BusinessRuleException;

    @PostMapping("/centros")
    @Operation(summary = "Cadastrar centro de armazenamento", description = "Cria um novo centro de armazenamento")
    ResponseEntity<Long> cadastrarCentro(@RequestBody CadastrarCentroRequest request) throws BusinessRuleException;

    @PutMapping("/centros/{id}")
    @Operation(summary = "Atualizar centro de armazenamento", description = "Atualiza os dados de um centro")
    ResponseEntity<Void> atualizarCentro(@PathVariable Long id, @RequestBody CadastrarCentroRequest request) throws BusinessRuleException;

    @DeleteMapping("/centros/{id}")
    @Operation(summary = "Desativar centro de armazenamento", description = "Desativa um centro (soft delete)")
    ResponseEntity<Void> desativarCentro(@PathVariable Long id) throws BusinessRuleException;

    // ============================================
    // LOCALIZAÇÕES
    // ============================================

    @GetMapping("/centros/{id}/localizacoes")
    @Operation(summary = "Listar localizações do centro", description = "Lista todas as localizações de um centro")
    ResponseEntity<List<LocalizacaoResponse>> listarLocalizacoesPorCentro(@PathVariable Long id) throws BusinessRuleException;

    @PostMapping("/centros/{id}/localizacoes")
    @Operation(summary = "Cadastrar localização", description = "Cadastra uma nova localização em um centro")
    ResponseEntity<Long> cadastrarLocalizacao(@PathVariable Long id, @RequestBody CadastrarLocalizacaoRequest request) throws BusinessRuleException;

    // ============================================
    // VÍNCULO PRODUTO ↔ LOCALIZAÇÃO
    // ============================================

    @PostMapping("/produtos/localizacoes")
    @Operation(summary = "Vincular produto a localização", description = "Vincula um produto a uma localização")
    ResponseEntity<Void> vincularProdutoLocalizacao(@RequestBody VincularProdutoLocalizacaoRequest request) throws BusinessRuleException;

    @GetMapping("/produtos/{id}/localizacoes")
    @Operation(summary = "Listar localizações do produto", description = "Lista todas as localizações onde o produto está armazenado")
    ResponseEntity<List<ProdutoLocalizacaoResponse>> listarLocalizacoesProduto(@PathVariable Long id) throws BusinessRuleException;

    // ============================================
    // PRODUTOS
    // ============================================

    @GetMapping("/produtos")
    @Operation(summary = "Listar produtos", description = "Lista todos os produtos ativos")
    ResponseEntity<List<ProdutoResponse>> listarProdutos() throws BusinessRuleException;

    @GetMapping("/produtos/{id}")
    @Operation(summary = "Buscar produto por ID", description = "Busca um produto pelo ID")
    ResponseEntity<ProdutoResponse> buscarProduto(@PathVariable Long id) throws BusinessRuleException;

    @PostMapping("/produtos")
    @Operation(summary = "Cadastrar produto", description = "Cadastra um novo produto no catálogo")
    ResponseEntity<Long> cadastrarProduto(@RequestBody CadastrarProdutoRequest request) throws BusinessRuleException;

    @PutMapping("/produtos/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto")
    ResponseEntity<Void> atualizarProduto(@PathVariable Long id, @RequestBody AtualizarProdutoRequest request) throws BusinessRuleException;

    @DeleteMapping("/produtos/{id}")
    @Operation(summary = "Desativar produto", description = "Desativa um produto (soft delete)")
    ResponseEntity<Void> desativarProduto(@PathVariable Long id) throws BusinessRuleException;

    // ============================================
    // CATEGORIAS
    // ============================================

    @GetMapping("/categorias")
    @Operation(summary = "Listar categorias", description = "Lista todas as categorias de produtos")
    ResponseEntity<List<CategoriaResponse>> listarCategorias() throws BusinessRuleException;

    // ============================================
    // ESTOQUE (MOVIMENTAÇÕES)
    // ============================================

    @PostMapping("/movimentacoes/entrada")
    @Operation(summary = "Registrar entrada de estoque", description = "Registra uma entrada de mercadoria (compra, transferência, devolução)")
    ResponseEntity<Long> registrarEntrada(@RequestBody RegistrarEntradaRequest request) throws BusinessRuleException;

    @PostMapping("/movimentacoes/saida")
    @Operation(summary = "Registrar saída de estoque", description = "Registra uma saída de mercadoria (venda, consumo, perda)")
    ResponseEntity<Void> registrarSaida(@RequestBody RegistrarSaidaRequest request) throws BusinessRuleException;

    @GetMapping("/movimentacoes")
    @Operation(summary = "Listar movimentações", description = "Lista movimentações com filtros por tipo e período")
    ResponseEntity<List<MovimentacaoResponse>> listarMovimentacoes(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) throws BusinessRuleException;

    @GetMapping("/produtos/{id}/movimentacoes")
    @Operation(summary = "Histórico de movimentações do produto", description = "Lista todas as movimentações de um produto")
    ResponseEntity<List<MovimentacaoResponse>> historicoProduto(@PathVariable Long id) throws BusinessRuleException;

    @GetMapping("/produtos/{id}/disponivel")
    @Operation(summary = "Estoque disponível", description = "Consulta o estoque disponível de um produto")
    ResponseEntity<EstoqueDisponivelResponse> estoqueDisponivel(@PathVariable Long id) throws BusinessRuleException;

    // ============================================
    // INVENTÁRIO
    // ============================================

    @PostMapping("/inventario")
    @Operation(summary = "Iniciar inventário", description = "Inicia um novo inventário para um produto")
    ResponseEntity<Long> cadastrarInventario(@RequestParam Long idProduto) throws BusinessRuleException;

    @PostMapping("/inventario/{id}/contagem")
    @Operation(summary = "Registrar contagem física", description = "Registra a contagem física de um item do inventário")
    ResponseEntity<Void> registrarContagem(@PathVariable Long id, @RequestBody RegistrarContagemRequest request) throws BusinessRuleException;

    @GetMapping("/inventario/divergencias")
    @Operation(summary = "Listar divergências", description = "Lista todas as divergências pendentes do inventário")
    ResponseEntity<List<InventarioResponse>> listarDivergencias() throws BusinessRuleException;

    @PutMapping("/inventario/{id}/ajustar")
    @Operation(summary = "Aplicar ajuste de inventário", description = "Aprova e aplica o ajuste baseado na contagem física")
    ResponseEntity<Void> ajustarInventario(@PathVariable Long id) throws BusinessRuleException;

    // ============================================
    // DASHBOARD
    // ============================================

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard de monitoramento", description = "Retorna indicadores gerais do estoque")
    ResponseEntity<DashboardResponse> buscarDashboard() throws BusinessRuleException;

    // ============================================
    // RELATÓRIOS
    // ============================================

    @GetMapping("/relatorios/valor")
    @Operation(summary = "Relatório de valor", description = "Valor total do estoque por categoria")
    ResponseEntity<List<RelatorioValorResponse>> relatorioValor() throws BusinessRuleException;

    @GetMapping("/relatorios/giro")
    @Operation(summary = "Relatório de giro", description = "Giro de estoque por produto")
    ResponseEntity<List<RelatorioGiroResponse>> relatorioGiro() throws BusinessRuleException;
}
