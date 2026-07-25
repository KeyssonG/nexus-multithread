package keysson.apis.estoque.repository;

import keysson.apis.estoque.dto.request.*;
import keysson.apis.estoque.dto.response.*;
import keysson.apis.estoque.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class EstoqueRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CentroArmazenamentoRowMapper centroMapper;

    @Autowired
    private LocalizacaoRowMapper localizacaoMapper;

    @Autowired
    private ProdutoLocalizacaoRowMapper produtoLocalizacaoMapper;

    @Autowired
    private ProdutoRowMapper produtoMapper;

    @Autowired
    private CategoriaRowMapper categoriaMapper;

    @Autowired
    private MovimentacaoRowMapper movimentacaoMapper;

    @Autowired
    private InventarioRowMapper inventarioMapper;

    // ============================================
    // CENTROS DE ARMAZENAMENTO
    // ============================================

    private static final String SQL_LISTAR_CENTROS = """
            SELECT id_centro, nome, descricao, tipo, endereco, cep, cidade, uf,
                   id_responsavel, status, criado_em, atualizado_em
            FROM tb_centro_armazenamento
            WHERE status = 'ATIVO'
            ORDER BY nome
            """;

    private static final String SQL_BUSCAR_CENTRO_POR_ID = """
            SELECT id_centro, nome, descricao, tipo, endereco, cep, cidade, uf,
                   id_responsavel, status, criado_em, atualizado_em
            FROM tb_centro_armazenamento
            WHERE id_centro = ?
            """;

    private static final String SQL_CADASTRAR_CENTRO = """
            INSERT INTO tb_centro_armazenamento (nome, descricao, tipo, endereco, cep, cidade, uf, id_responsavel, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ATUALIZAR_CENTRO = """
            UPDATE tb_centro_armazenamento
            SET nome = ?, descricao = ?, tipo = ?, endereco = ?, cep = ?, cidade = ?,
                uf = ?, id_responsavel = ?, status = ?
            WHERE id_centro = ?
            """;

    private static final String SQL_DESATIVAR_CENTRO = """
            UPDATE tb_centro_armazenamento SET status = 'INATIVO' WHERE id_centro = ?
            """;

    public List<CentroArmazenamentoResponse> listarCentros() {
        return jdbcTemplate.query(SQL_LISTAR_CENTROS, centroMapper);
    }

    public CentroArmazenamentoResponse buscarCentroPorId(Long id) {
        List<CentroArmazenamentoResponse> resultados = jdbcTemplate.query(
                SQL_BUSCAR_CENTRO_POR_ID, new Object[]{id}, centroMapper);
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    public Long cadastrarCentro(CadastrarCentroRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_CENTRO, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.nome());
            ps.setString(2, request.descricao());
            ps.setString(3, request.tipo());
            ps.setString(4, request.endereco());
            ps.setString(5, request.cep());
            ps.setString(6, request.cidade());
            ps.setString(7, request.uf());
            if (request.idResponsavel() != null) {
                ps.setLong(8, request.idResponsavel());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            ps.setString(9, request.status() != null ? request.status() : "ATIVO");
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_centro")).longValue();
    }

    public void atualizarCentro(Long id, CadastrarCentroRequest request) {
        jdbcTemplate.update(SQL_ATUALIZAR_CENTRO,
                request.nome(), request.descricao(), request.tipo(), request.endereco(),
                request.cep(), request.cidade(), request.uf(), request.idResponsavel(),
                request.status() != null ? request.status() : "ATIVO", id);
    }

    public void desativarCentro(Long id) {
        jdbcTemplate.update(SQL_DESATIVAR_CENTRO, id);
    }

    // ============================================
    // LOCALIZAÇÕES
    // ============================================

    private static final String SQL_LISTAR_LOCALIZACOES_POR_CENTRO = """
            SELECT id_localizacao, id_centro, codigo, descricao, corredor, prateleira,
                   nivel, capacidade_max, status, criado_em
            FROM tb_localizacao
            WHERE id_centro = ?
            ORDER BY codigo
            """;

    private static final String SQL_CADASTRAR_LOCALIZACAO = """
            INSERT INTO tb_localizacao (id_centro, codigo, descricao, corredor, prateleira, nivel, capacidade_max, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public List<LocalizacaoResponse> listarLocalizacoesPorCentro(Long idCentro) {
        return jdbcTemplate.query(SQL_LISTAR_LOCALIZACOES_POR_CENTRO, new Object[]{idCentro}, localizacaoMapper);
    }

    public Long cadastrarLocalizacao(Long idCentro, CadastrarLocalizacaoRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_LOCALIZACAO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idCentro);
            ps.setString(2, request.codigo());
            ps.setString(3, request.descricao());
            ps.setString(4, request.corredor());
            ps.setString(5, request.prateleira());
            ps.setString(6, request.nivel());
            if (request.capacidadeMax() != null) {
                ps.setInt(7, request.capacidadeMax());
            } else {
                ps.setNull(7, Types.INTEGER);
            }
            ps.setString(8, request.status() != null ? request.status() : "ATIVA");
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_localizacao")).longValue();
    }

    // ============================================
    // VÍNCULO PRODUTO ↔ LOCALIZAÇÃO
    // ============================================

    private static final String SQL_LISTAR_LOCALIZACOES_PRODUTO = """
            SELECT pl.id_produto_localizacao, pl.id_produto, pl.id_localizacao,
                   l.codigo AS codigo_localizacao, pl.quantidade, pl.criado_em
            FROM tb_produto_localizacao pl
            JOIN tb_localizacao l ON pl.id_localizacao = l.id_localizacao
            WHERE pl.id_produto = ?
            """;

    private static final String SQL_VINCULAR_PRODUTO_LOCALIZACAO = """
            INSERT INTO tb_produto_localizacao (id_produto, id_localizacao, quantidade)
            VALUES (?, ?, ?)
            """;

    public List<ProdutoLocalizacaoResponse> listarLocalizacoesProduto(Long idProduto) {
        return jdbcTemplate.query(SQL_LISTAR_LOCALIZACOES_PRODUTO, new Object[]{idProduto}, produtoLocalizacaoMapper);
    }

    public void vincularProdutoLocalizacao(VincularProdutoLocalizacaoRequest request) {
        jdbcTemplate.update(SQL_VINCULAR_PRODUTO_LOCALIZACAO,
                request.idProduto(), request.idLocalizacao(),
                request.quantidade() != null ? request.quantidade() : 0);
    }

    // ============================================
    // PRODUTOS
    // ============================================

    private static final String SQL_LISTAR_PRODUTOS = """
            SELECT p.id_produto, p.nome, p.descricao, p.id_categoria, c.nome AS categoria_nome,
                   p.id_fornecedor, p.id_centro_padrao, ca.nome AS centro_padrao_nome,
                   p.unidade_medida, p.preco_custo, p.preco_venda,
                   p.qtd_estoque_atual, p.qtd_estoque_minimo, p.qtd_estoque_maximo,
                   p.status, p.criado_em, p.atualizado_em
            FROM tb_produto p
            LEFT JOIN tb_categoria c ON p.id_categoria = c.id_categoria
            LEFT JOIN tb_centro_armazenamento ca ON p.id_centro_padrao = ca.id_centro
            WHERE p.status = 'ATIVO'
            ORDER BY p.nome
            """;

    private static final String SQL_BUSCAR_PRODUTO_POR_ID = """
            SELECT p.id_produto, p.nome, p.descricao, p.id_categoria, c.nome AS categoria_nome,
                   p.id_fornecedor, p.id_centro_padrao, ca.nome AS centro_padrao_nome,
                   p.unidade_medida, p.preco_custo, p.preco_venda,
                   p.qtd_estoque_atual, p.qtd_estoque_minimo, p.qtd_estoque_maximo,
                   p.status, p.criado_em, p.atualizado_em
            FROM tb_produto p
            LEFT JOIN tb_categoria c ON p.id_categoria = c.id_categoria
            LEFT JOIN tb_centro_armazenamento ca ON p.id_centro_padrao = ca.id_centro
            WHERE p.id_produto = ?
            """;

    private static final String SQL_CADASTRAR_PRODUTO = """
            INSERT INTO tb_produto (nome, descricao, id_categoria, id_fornecedor, id_centro_padrao,
                                    unidade_medida, preco_custo, preco_venda,
                                    qtd_estoque_minimo, qtd_estoque_maximo, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ATUALIZAR_PRODUTO = """
            UPDATE tb_produto
            SET nome = ?, descricao = ?, id_categoria = ?, id_fornecedor = ?, id_centro_padrao = ?,
                unidade_medida = ?, preco_custo = ?, preco_venda = ?,
                qtd_estoque_minimo = ?, qtd_estoque_maximo = ?, status = ?
            WHERE id_produto = ?
            """;

    private static final String SQL_DESATIVAR_PRODUTO = """
            UPDATE tb_produto SET status = 'INATIVO' WHERE id_produto = ?
            """;

    public List<ProdutoResponse> listarProdutos() {
        return jdbcTemplate.query(SQL_LISTAR_PRODUTOS, produtoMapper);
    }

    public ProdutoResponse buscarProdutoPorId(Long id) {
        List<ProdutoResponse> resultados = jdbcTemplate.query(
                SQL_BUSCAR_PRODUTO_POR_ID, new Object[]{id}, produtoMapper);
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    public Long cadastrarProduto(CadastrarProdutoRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_PRODUTO, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.nome());
            ps.setString(2, request.descricao());
            ps.setLong(3, request.idCategoria());
            if (request.idFornecedor() != null) {
                ps.setLong(4, request.idFornecedor());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            ps.setLong(5, request.idCentroPadrao());
            ps.setString(6, request.unidadeMedida());
            ps.setBigDecimal(7, request.precoCusto());
            ps.setBigDecimal(8, request.precoVenda());
            ps.setInt(9, request.qtdEstoqueMinimo());
            ps.setInt(10, request.qtdEstoqueMaximo());
            ps.setString(11, request.status() != null ? request.status() : "ATIVO");
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_produto")).longValue();
    }

    public void atualizarProduto(Long id, AtualizarProdutoRequest request) {
        ProdutoResponse atual = buscarProdutoPorId(id);
        if (atual == null) return;

        jdbcTemplate.update(SQL_ATUALIZAR_PRODUTO,
                request.nome() != null ? request.nome() : atual.nome(),
                request.descricao() != null ? request.descricao() : atual.descricao(),
                request.idCategoria() != null ? request.idCategoria() : atual.idCategoria(),
                request.idFornecedor() != null ? request.idFornecedor() : atual.idFornecedor(),
                request.idCentroPadrao() != null ? request.idCentroPadrao() : atual.idCentroPadrao(),
                request.unidadeMedida() != null ? request.unidadeMedida() : atual.unidadeMedida(),
                request.precoCusto() != null ? request.precoCusto() : atual.precoCusto(),
                request.precoVenda() != null ? request.precoVenda() : atual.precoVenda(),
                request.qtdEstoqueMinimo() != null ? request.qtdEstoqueMinimo() : atual.qtdEstoqueMinimo(),
                request.qtdEstoqueMaximo() != null ? request.qtdEstoqueMaximo() : atual.qtdEstoqueMaximo(),
                request.status() != null ? request.status() : atual.status(),
                id);
    }

    public void desativarProduto(Long id) {
        jdbcTemplate.update(SQL_DESATIVAR_PRODUTO, id);
    }

    // ============================================
    // CATEGORIAS
    // ============================================

    private static final String SQL_LISTAR_CATEGORIAS = """
            SELECT id_categoria, nome, descricao, criado_em
            FROM tb_categoria
            ORDER BY nome
            """;

    public List<CategoriaResponse> listarCategorias() {
        return jdbcTemplate.query(SQL_LISTAR_CATEGORIAS, categoriaMapper);
    }

    // ============================================
    // MOVIMENTAÇÕES DE ESTOQUE
    // ============================================

    private static final String SQL_LISTAR_MOVIMENTACOES = """
            SELECT m.id_movimentacao, m.id_produto, p.nome AS produto_nome,
                   m.tipo, m.origem, m.quantidade, m.numero_nf, m.lote,
                   m.validade, m.observacao, m.id_usuario, m.criado_em
            FROM tb_movimentacao_estoque m
            JOIN tb_produto p ON m.id_produto = p.id_produto
            WHERE 1=1
            """;

    private static final String SQL_HISTORICO_PRODUTO = """
            SELECT m.id_movimentacao, m.id_produto, p.nome AS produto_nome,
                   m.tipo, m.origem, m.quantidade, m.numero_nf, m.lote,
                   m.validade, m.observacao, m.id_usuario, m.criado_em
            FROM tb_movimentacao_estoque m
            JOIN tb_produto p ON m.id_produto = p.id_produto
            WHERE m.id_produto = ?
            ORDER BY m.criado_em DESC
            """;

    private static final String SQL_REGISTRAR_MOVIMENTACAO = """
            INSERT INTO tb_movimentacao_estoque (id_produto, tipo, origem, quantidade, numero_nf, lote, validade, observacao, id_usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ATUALIZAR_ESTOQUE_ENTRADA = """
            UPDATE tb_produto SET qtd_estoque_atual = qtd_estoque_atual + ? WHERE id_produto = ?
            """;

    private static final String SQL_ATUALIZAR_ESTOQUE_SAIDA = """
            UPDATE tb_produto SET qtd_estoque_atual = qtd_estoque_atual - ? WHERE id_produto = ?
            """;

    private static final String SQL_VERIFICAR_ESTOQUE = """
            SELECT qtd_estoque_atual FROM tb_produto WHERE id_produto = ?
            """;

    private static final String SQL_ESTOQUE_DISPONIVEL = """
            SELECT p.id_produto, p.nome AS produto_nome, p.qtd_estoque_atual,
                   p.qtd_estoque_minimo, p.qtd_estoque_maximo,
                   CASE
                       WHEN p.qtd_estoque_atual = 0 THEN 'CRITICO'
                       WHEN p.qtd_estoque_atual <= p.qtd_estoque_minimo THEN 'BAIXO'
                       ELSE 'OK'
                   END AS status_estoque,
                   (p.qtd_estoque_atual * p.preco_custo) AS valor_total
            FROM tb_produto p
            WHERE p.id_produto = ? AND p.status = 'ATIVO'
            """;

    public List<MovimentacaoResponse> listarMovimentacoes(String tipo, LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder sql = new StringBuilder(SQL_LISTAR_MOVIMENTACOES);
        List<Object> params = new ArrayList<>();

        if (tipo != null && !tipo.isEmpty()) {
            sql.append(" AND m.tipo = ?");
            params.add(tipo);
        }
        if (dataInicio != null) {
            sql.append(" AND m.criado_em >= ?");
            params.add(dataInicio.atStartOfDay());
        }
        if (dataFim != null) {
            sql.append(" AND m.criado_em <= ?");
            params.add(dataFim.atTime(23, 59, 59));
        }

        sql.append(" ORDER BY m.criado_em DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), movimentacaoMapper);
    }

    public List<MovimentacaoResponse> historicoProduto(Long idProduto) {
        return jdbcTemplate.query(SQL_HISTORICO_PRODUTO, new Object[]{idProduto}, movimentacaoMapper);
    }

    public Long registrarEntrada(RegistrarEntradaRequest request, Long idUsuario) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_REGISTRAR_MOVIMENTACAO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.idProduto());
            ps.setString(2, "ENTRADA");
            ps.setString(3, request.origem());
            ps.setInt(4, request.quantidade());
            ps.setString(5, request.numeroNf());
            ps.setString(6, request.lote());
            if (request.validade() != null) {
                ps.setDate(7, java.sql.Date.valueOf(request.validade()));
            } else {
                ps.setNull(7, Types.DATE);
            }
            ps.setString(8, request.observacao());
            ps.setLong(9, idUsuario);
            return ps;
        }, keyHolder);

        jdbcTemplate.update(SQL_ATUALIZAR_ESTOQUE_ENTRADA, request.quantidade(), request.idProduto());

        return ((Number) keyHolder.getKeyList().getFirst().get("id_movimentacao")).longValue();
    }

    public void registrarSaida(RegistrarSaidaRequest request, Long idUsuario) {
        Integer estoqueAtual = jdbcTemplate.queryForObject(
                SQL_VERIFICAR_ESTOQUE, new Object[]{request.idProduto()}, Integer.class);

        if (estoqueAtual == null || estoqueAtual < request.quantidade()) {
            throw new keysson.apis.estoque.exception.BusinessRuleException(
                    keysson.apis.estoque.exception.enums.ErrorCode.ERROR_ESTOQUE_INSUFICIENTE);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_REGISTRAR_MOVIMENTACAO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.idProduto());
            ps.setString(2, "SAIDA");
            ps.setString(3, request.origem());
            ps.setInt(4, request.quantidade());
            ps.setNull(5, Types.VARCHAR);
            ps.setNull(6, Types.VARCHAR);
            ps.setNull(7, Types.DATE);
            ps.setString(8, request.observacao());
            ps.setLong(9, idUsuario);
            return ps;
        }, keyHolder);

        jdbcTemplate.update(SQL_ATUALIZAR_ESTOQUE_SAIDA, request.quantidade(), request.idProduto());
    }

    public EstoqueDisponivelResponse estoqueDisponivel(Long idProduto) {
        List<EstoqueDisponivelResponse> resultados = jdbcTemplate.query(
                SQL_ESTOQUE_DISPONIVEL, new Object[]{idProduto},
                (rs, rowNum) -> new EstoqueDisponivelResponse(
                        rs.getLong("id_produto"),
                        rs.getString("produto_nome"),
                        rs.getInt("qtd_estoque_atual"),
                        rs.getInt("qtd_estoque_minimo"),
                        rs.getInt("qtd_estoque_maximo"),
                        rs.getString("status_estoque"),
                        rs.getBigDecimal("valor_total")
                ));
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    // ============================================
    // INVENTÁRIO
    // ============================================

    private static final String SQL_CADASTRAR_INVENTARIO = """
            INSERT INTO tb_inventario (id_produto, qtd_sistema, qtd_fisica, divergencia, status, id_usuario)
            VALUES (?, ?, ?, ?, 'PENDENTE', ?)
            """;

    private static final String SQL_LISTAR_DIVERGENCIAS = """
            SELECT i.id_inventario, i.id_produto, p.nome AS produto_nome,
                   i.qtd_sistema, i.qtd_fisica, i.divergencia, i.status, i.id_usuario,
                   i.observacao, i.criado_em
            FROM tb_inventario i
            JOIN tb_produto p ON i.id_produto = p.id_produto
            WHERE i.status = 'PENDENTE'
            ORDER BY i.criado_em DESC
            """;

    private static final String SQL_AJUSTAR_INVENTARIO = """
            UPDATE tb_inventario SET status = 'AJUSTADO' WHERE id_inventario = ?
            """;

    private static final String SQL_ATUALIZAR_ESTOQUE_POR_INVENTARIO = """
            UPDATE tb_produto SET qtd_estoque_atual = ? WHERE id_produto = ?
            """;

    private static final String SQL_BUSCAR_INVENTARIO_POR_ID = """
            SELECT i.id_inventario, i.id_produto, p.nome AS produto_nome,
                   i.qtd_sistema, i.qtd_fisica, i.divergencia, i.status, i.id_usuario,
                   i.observacao, i.criado_em
            FROM tb_inventario i
            JOIN tb_produto p ON i.id_produto = p.id_produto
            WHERE i.id_inventario = ?
            """;

    public Long cadastrarInventario(Long idProduto, Long idUsuario) {
        Integer qtdSistemaRaw = jdbcTemplate.queryForObject(
                SQL_VERIFICAR_ESTOQUE, new Object[]{idProduto}, Integer.class);
        int qtdSistema = qtdSistemaRaw != null ? qtdSistemaRaw : 0;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_INVENTARIO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idProduto);
            ps.setInt(2, qtdSistema);
            ps.setNull(3, Types.INTEGER);
            ps.setNull(4, Types.INTEGER);
            ps.setLong(5, idUsuario);
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_inventario")).longValue();
    }

    public void registrarContagem(Long idInventario, Integer qtdFisica, String observacao) {
        InventarioResponse inv = buscarInventarioPorId(idInventario);
        if (inv == null) return;

        int divergencia = qtdFisica - inv.qtdSistema();
        String sql = "UPDATE tb_inventario SET qtd_fisica = ?, divergencia = ?, observacao = ? WHERE id_inventario = ?";
        jdbcTemplate.update(sql, qtdFisica, divergencia, observacao, idInventario);
    }

    public InventarioResponse buscarInventarioPorId(Long id) {
        List<InventarioResponse> resultados = jdbcTemplate.query(
                SQL_BUSCAR_INVENTARIO_POR_ID, new Object[]{id}, inventarioMapper);
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    public List<InventarioResponse> listarDivergencias() {
        return jdbcTemplate.query(SQL_LISTAR_DIVERGENCIAS, inventarioMapper);
    }

    public void ajustarInventario(Long idInventario) {
        InventarioResponse inv = buscarInventarioPorId(idInventario);
        if (inv == null) return;

        jdbcTemplate.update(SQL_ATUALIZAR_ESTOQUE_POR_INVENTARIO, inv.qtdFisica(), inv.idProduto());
        jdbcTemplate.update(SQL_AJUSTAR_INVENTARIO, idInventario);
    }

    // ============================================
    // DASHBOARD
    // ============================================

    private static final String SQL_DASHBOARD_TOTAL_ITENS = """
            SELECT COUNT(*) FROM tb_produto WHERE status = 'ATIVO'
            """;

    private static final String SQL_DASHBOARD_ESTOQUE_BAIXO = """
            SELECT COUNT(*) FROM tb_produto
            WHERE status = 'ATIVO' AND qtd_estoque_atual > 0 AND qtd_estoque_atual <= qtd_estoque_minimo
            """;

    private static final String SQL_DASHBOARD_ALERTAS_CRITICOS = """
            SELECT COUNT(*) FROM tb_produto
            WHERE status = 'ATIVO' AND (qtd_estoque_atual = 0 OR qtd_estoque_atual < 0)
            """;

    private static final String SQL_DASHBOARD_VALOR_TOTAL = """
            SELECT COALESCE(SUM(qtd_estoque_atual * preco_custo), 0) FROM tb_produto WHERE status = 'ATIVO'
            """;

    public DashboardResponse buscarDashboard() {
        Long totalItens = jdbcTemplate.queryForObject(SQL_DASHBOARD_TOTAL_ITENS, Long.class);
        Long estoqueBaixo = jdbcTemplate.queryForObject(SQL_DASHBOARD_ESTOQUE_BAIXO, Long.class);
        Long alertasCriticos = jdbcTemplate.queryForObject(SQL_DASHBOARD_ALERTAS_CRITICOS, Long.class);
        BigDecimal valorTotal = jdbcTemplate.queryForObject(SQL_DASHBOARD_VALOR_TOTAL, BigDecimal.class);

        return new DashboardResponse(totalItens, estoqueBaixo, alertasCriticos, valorTotal);
    }

    // ============================================
    // RELATÓRIOS
    // ============================================

    private static final String SQL_RELATORIO_VALOR = """
            SELECT c.id_categoria, c.nome AS categoria_nome,
                   COUNT(p.id_produto) AS total_produtos,
                   COALESCE(SUM(p.qtd_estoque_atual), 0) AS quantidade_total,
                   COALESCE(SUM(p.qtd_estoque_atual * p.preco_custo), 0) AS valor_total
            FROM tb_produto p
            JOIN tb_categoria c ON p.id_categoria = c.id_categoria
            WHERE p.status = 'ATIVO'
            GROUP BY c.id_categoria, c.nome
            ORDER BY valor_total DESC
            """;

    private static final String SQL_RELATORIO_GIRO = """
            SELECT p.id_produto, p.nome AS produto_nome, c.nome AS categoria_nome,
                   p.qtd_estoque_atual,
                   COALESCE(entradas.total, 0) AS total_entradas,
                   COALESCE(saidas.total, 0) AS total_saidas,
                   CASE
                       WHEN p.qtd_estoque_atual > 0
                       THEN ROUND(COALESCE(saidas.total, 0)::DECIMAL / p.qtd_estoque_atual, 2)
                       ELSE 0
                   END AS giro_estoque
            FROM tb_produto p
            JOIN tb_categoria c ON p.id_categoria = c.id_categoria
            LEFT JOIN (
                SELECT id_produto, SUM(quantidade) AS total
                FROM tb_movimentacao_estoque WHERE tipo = 'ENTRADA'
                GROUP BY id_produto
            ) entradas ON p.id_produto = entradas.id_produto
            LEFT JOIN (
                SELECT id_produto, SUM(quantidade) AS total
                FROM tb_movimentacao_estoque WHERE tipo = 'SAIDA'
                GROUP BY id_produto
            ) saidas ON p.id_produto = saidas.id_produto
            WHERE p.status = 'ATIVO'
            ORDER BY giro_estoque DESC
            """;

    public List<RelatorioValorResponse> relatorioValor() {
        return jdbcTemplate.query(SQL_RELATORIO_VALOR,
                (rs, rowNum) -> new RelatorioValorResponse(
                        rs.getLong("id_categoria"),
                        rs.getString("categoria_nome"),
                        rs.getInt("total_produtos"),
                        rs.getInt("quantidade_total"),
                        rs.getBigDecimal("valor_total")
                ));
    }

    public List<RelatorioGiroResponse> relatorioGiro() {
        return jdbcTemplate.query(SQL_RELATORIO_GIRO,
                (rs, rowNum) -> new RelatorioGiroResponse(
                        rs.getLong("id_produto"),
                        rs.getString("produto_nome"),
                        rs.getString("categoria_nome"),
                        rs.getInt("qtd_estoque_atual"),
                        rs.getInt("total_entradas"),
                        rs.getInt("total_saidas"),
                        rs.getBigDecimal("giro_estoque")
                ));
    }
}
