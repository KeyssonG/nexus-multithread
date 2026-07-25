package keysson.apis.estoque.service;

import jakarta.servlet.http.HttpServletRequest;
import keysson.apis.estoque.dto.request.*;
import keysson.apis.estoque.dto.response.*;
import keysson.apis.estoque.exception.BusinessRuleException;
import keysson.apis.estoque.repository.EstoqueRepository;
import keysson.nexus.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static keysson.apis.estoque.exception.enums.ErrorCode.*;

@Service
@Slf4j
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    public EstoqueService(EstoqueRepository estoqueRepository) {
        this.estoqueRepository = estoqueRepository;
    }

    private Long extrairIdUsuario() {
        String token = (String) httpRequest.getAttribute("CleanJwt");
        return jwtUtil.extractCompanyId(token).longValue();
    }

    // ============================================
    // CENTROS DE ARMAZENAMENTO
    // ============================================

    public List<CentroArmazenamentoResponse> listarCentros() {
        log.info("Listando centros de armazenamento");
        return estoqueRepository.listarCentros();
    }

    public CentroArmazenamentoResponse buscarCentro(Long id) throws BusinessRuleException {
        log.info("Buscando centro de armazenamento ID: {}", id);
        CentroArmazenamentoResponse centro = estoqueRepository.buscarCentroPorId(id);
        if (centro == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_CENTRO);
        }
        return centro;
    }

    @Transactional
    public Long cadastrarCentro(CadastrarCentroRequest request) throws BusinessRuleException {
        log.info("Cadastrando centro de armazenamento: {}", request.nome());
        try {
            return estoqueRepository.cadastrarCentro(request);
        } catch (Exception e) {
            log.error("Erro ao cadastrar centro: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_CADASTRAR_CENTRO);
        }
    }

    @Transactional
    public void atualizarCentro(Long id, CadastrarCentroRequest request) throws BusinessRuleException {
        log.info("Atualizando centro de armazenamento ID: {}", id);
        CentroArmazenamentoResponse centro = estoqueRepository.buscarCentroPorId(id);
        if (centro == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_CENTRO);
        }
        try {
            estoqueRepository.atualizarCentro(id, request);
        } catch (Exception e) {
            log.error("Erro ao atualizar centro ID {}: {}", id, e.getMessage());
            throw new BusinessRuleException(ERROR_ATUALIZAR_CENTRO);
        }
    }

    @Transactional
    public void desativarCentro(Long id) throws BusinessRuleException {
        log.info("Desativando centro de armazenamento ID: {}", id);
        CentroArmazenamentoResponse centro = estoqueRepository.buscarCentroPorId(id);
        if (centro == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_CENTRO);
        }
        try {
            estoqueRepository.desativarCentro(id);
        } catch (Exception e) {
            log.error("Erro ao desativar centro ID {}: {}", id, e.getMessage());
            throw new BusinessRuleException(ERROR_DESATIVAR_CENTRO);
        }
    }

    // ============================================
    // LOCALIZAÇÕES
    // ============================================

    public List<LocalizacaoResponse> listarLocalizacoesPorCentro(Long idCentro) {
        log.info("Listando localizações do centro ID: {}", idCentro);
        return estoqueRepository.listarLocalizacoesPorCentro(idCentro);
    }

    @Transactional
    public Long cadastrarLocalizacao(Long idCentro, CadastrarLocalizacaoRequest request) throws BusinessRuleException {
        log.info("Cadastrando localização '{}' no centro ID: {}", request.codigo(), idCentro);
        try {
            return estoqueRepository.cadastrarLocalizacao(idCentro, request);
        } catch (Exception e) {
            log.error("Erro ao cadastrar localização: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_CADASTRAR_LOCALIZACAO);
        }
    }

    // ============================================
    // VÍNCULO PRODUTO ↔ LOCALIZAÇÃO
    // ============================================

    public List<ProdutoLocalizacaoResponse> listarLocalizacoesProduto(Long idProduto) {
        log.info("Listando localizações do produto ID: {}", idProduto);
        return estoqueRepository.listarLocalizacoesProduto(idProduto);
    }

    @Transactional
    public void vincularProdutoLocalizacao(VincularProdutoLocalizacaoRequest request) throws BusinessRuleException {
        log.info("Vinculando produto {} à localização {}", request.idProduto(), request.idLocalizacao());
        try {
            estoqueRepository.vincularProdutoLocalizacao(request);
        } catch (Exception e) {
            log.error("Erro ao vincular produto à localização: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_VINCULAR_PRODUTO_LOCALIZACAO);
        }
    }

    // ============================================
    // PRODUTOS
    // ============================================

    public List<ProdutoResponse> listarProdutos() {
        log.info("Listando produtos");
        return estoqueRepository.listarProdutos();
    }

    public ProdutoResponse buscarProduto(Long id) throws BusinessRuleException {
        log.info("Buscando produto ID: {}", id);
        ProdutoResponse produto = estoqueRepository.buscarProdutoPorId(id);
        if (produto == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_PRODUTO);
        }
        return produto;
    }

    @Transactional
    public Long cadastrarProduto(CadastrarProdutoRequest request) throws BusinessRuleException {
        log.info("Cadastrando produto: {}", request.nome());
        try {
            return estoqueRepository.cadastrarProduto(request);
        } catch (Exception e) {
            log.error("Erro ao cadastrar produto: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_CADASTRAR_PRODUTO);
        }
    }

    @Transactional
    public void atualizarProduto(Long id, AtualizarProdutoRequest request) throws BusinessRuleException {
        log.info("Atualizando produto ID: {}", id);
        try {
            estoqueRepository.atualizarProduto(id, request);
        } catch (Exception e) {
            log.error("Erro ao atualizar produto ID {}: {}", id, e.getMessage());
            throw new BusinessRuleException(ERROR_ATUALIZAR_PRODUTO);
        }
    }

    @Transactional
    public void desativarProduto(Long id) throws BusinessRuleException {
        log.info("Desativando produto ID: {}", id);
        ProdutoResponse produto = estoqueRepository.buscarProdutoPorId(id);
        if (produto == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_PRODUTO);
        }
        try {
            estoqueRepository.desativarProduto(id);
        } catch (Exception e) {
            log.error("Erro ao desativar produto ID {}: {}", id, e.getMessage());
            throw new BusinessRuleException(ERROR_DESATIVAR_PRODUTO);
        }
    }

    // ============================================
    // CATEGORIAS
    // ============================================

    public List<CategoriaResponse> listarCategorias() {
        log.info("Listando categorias");
        return estoqueRepository.listarCategorias();
    }

    public CategoriaResponse buscarCategoria(Long id) throws BusinessRuleException {
        log.info("Buscando categoria ID: {}", id);
        CategoriaResponse categoria = estoqueRepository.buscarCategoriaPorId(id);
        if (categoria == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_CATEGORIA);
        }
        return categoria;
    }

    @Transactional
    public Long cadastrarCategoria(CadastrarCategoriaRequest request) throws BusinessRuleException {
        log.info("Cadastrando categoria: {}", request.nome());
        try {
            return estoqueRepository.cadastrarCategoria(request);
        } catch (Exception e) {
            log.error("Erro ao cadastrar categoria: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_CADASTRAR_CATEGORIA);
        }
    }

    @Transactional
    public void atualizarCategoria(Long id, CadastrarCategoriaRequest request) throws BusinessRuleException {
        log.info("Atualizando categoria ID: {}", id);
        CategoriaResponse categoria = estoqueRepository.buscarCategoriaPorId(id);
        if (categoria == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_CATEGORIA);
        }
        try {
            estoqueRepository.atualizarCategoria(id, request);
        } catch (Exception e) {
            log.error("Erro ao atualizar categoria ID {}: {}", id, e.getMessage());
            throw new BusinessRuleException(ERROR_ATUALIZAR_CATEGORIA);
        }
    }

    @Transactional
    public void desativarCategoria(Long id) throws BusinessRuleException {
        log.info("Desativando categoria ID: {}", id);
        CategoriaResponse categoria = estoqueRepository.buscarCategoriaPorId(id);
        if (categoria == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_CATEGORIA);
        }
        try {
            estoqueRepository.desativarCategoria(id);
        } catch (Exception e) {
            log.error("Erro ao desativar categoria ID {}: {}", id, e.getMessage());
            throw new BusinessRuleException(ERROR_DESATIVAR_CATEGORIA);
        }
    }

    // ============================================
    // MOVIMENTAÇÕES DE ESTOQUE
    // ============================================

    @Transactional
    public Long registrarEntrada(RegistrarEntradaRequest request) throws BusinessRuleException {
        log.info("Registrando entrada de estoque: produto={}, quantidade={}", request.idProduto(), request.quantidade());
        try {
            Long idUsuario = extrairIdUsuario();
            return estoqueRepository.registrarEntrada(request, idUsuario);
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao registrar entrada: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_REGISTRAR_ENTRADA);
        }
    }

    @Transactional
    public void registrarSaida(RegistrarSaidaRequest request) throws BusinessRuleException {
        log.info("Registrando saída de estoque: produto={}, quantidade={}", request.idProduto(), request.quantidade());
        try {
            Long idUsuario = extrairIdUsuario();
            estoqueRepository.registrarSaida(request, idUsuario);
        } catch (BusinessRuleException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao registrar saída: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_REGISTRAR_SAIDA);
        }
    }

    public List<MovimentacaoResponse> listarMovimentacoes(String tipo, LocalDate dataInicio, LocalDate dataFim) {
        log.info("Listando movimentações: tipo={}, período={}/{}", tipo, dataInicio, dataFim);
        return estoqueRepository.listarMovimentacoes(tipo, dataInicio, dataFim);
    }

    public List<MovimentacaoResponse> historicoProduto(Long idProduto) {
        log.info("Buscando histórico de movimentações do produto ID: {}", idProduto);
        return estoqueRepository.historicoProduto(idProduto);
    }

    public EstoqueDisponivelResponse estoqueDisponivel(Long idProduto) throws BusinessRuleException {
        log.info("Consultando estoque disponível do produto ID: {}", idProduto);
        EstoqueDisponivelResponse estoque = estoqueRepository.estoqueDisponivel(idProduto);
        if (estoque == null) {
            throw new BusinessRuleException(ERROR_BUSCAR_PRODUTO);
        }
        return estoque;
    }

    // ============================================
    // INVENTÁRIO
    // ============================================

    @Transactional
    public Long cadastrarInventario(Long idProduto) throws BusinessRuleException {
        log.info("Iniciando inventário para produto ID: {}", idProduto);
        try {
            Long idUsuario = extrairIdUsuario();
            return estoqueRepository.cadastrarInventario(idProduto, idUsuario);
        } catch (Exception e) {
            log.error("Erro ao cadastrar inventário: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_CADASTRAR_INVENTARIO);
        }
    }

    @Transactional
    public void registrarContagem(Long idInventario, RegistrarContagemRequest request) throws BusinessRuleException {
        log.info("Registrando contagem física no inventário ID: {}", idInventario);
        try {
            estoqueRepository.registrarContagem(idInventario, request.qtdFisica(), request.observacao());
        } catch (Exception e) {
            log.error("Erro ao registrar contagem: {}", e.getMessage());
            throw new BusinessRuleException(ERROR_REGISTRAR_CONTAGEM);
        }
    }

    public List<InventarioResponse> listarDivergencias() {
        log.info("Listando divergências do inventário");
        return estoqueRepository.listarDivergencias();
    }

    @Transactional
    public void ajustarInventario(Long idInventario) throws BusinessRuleException {
        log.info("Aplicando ajuste no inventário ID: {}", idInventario);
        try {
            estoqueRepository.ajustarInventario(idInventario);
        } catch (Exception e) {
            log.error("Erro ao ajustar inventário ID {}: {}", idInventario, e.getMessage());
            throw new BusinessRuleException(ERROR_AJUSTAR_INVENTARIO);
        }
    }

    // ============================================
    // DASHBOARD
    // ============================================

    public DashboardResponse buscarDashboard() {
        log.info("Buscando dados do dashboard");
        return estoqueRepository.buscarDashboard();
    }

    // ============================================
    // RELATÓRIOS
    // ============================================

    public List<RelatorioValorResponse> relatorioValor() {
        log.info("Gerando relatório de valor do estoque");
        return estoqueRepository.relatorioValor();
    }

    public List<RelatorioGiroResponse> relatorioGiro() {
        log.info("Gerando relatório de giro de estoque");
        return estoqueRepository.relatorioGiro();
    }
}
