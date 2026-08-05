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
            WHERE status = 'ATIVO' AND id_empresa = ?
            ORDER BY nome
            """;

    private static final String SQL_BUSCAR_CENTRO_POR_ID = """
            SELECT id_centro, nome, descricao, tipo, endereco, cep, cidade, uf,
                   id_responsavel, status, criado_em, atualizado_em
            FROM tb_centro_armazenamento
            WHERE id_centro = ? AND id_empresa = ?
            """;

    private static final String SQL_CADASTRAR_CENTRO = """
            INSERT INTO tb_centro_armazenamento (id_empresa, nome, descricao, tipo, endereco, cep, cidade, uf, id_responsavel, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ATUALIZAR_CENTRO = """
            UPDATE tb_centro_armazenamento
            SET nome = ?, descricao = ?, tipo = ?, endereco = ?, cep = ?, cidade = ?,
                uf = ?, id_responsavel = ?, status = ?
            WHERE id_centro = ? AND id_empresa = ?
            """;

    private static final String SQL_DESATIVAR_CENTRO = """
            UPDATE tb_centro_armazenamento SET status = 'INATIVO' WHERE id_centro = ? AND id_empresa = ?
            """;

    public List<CentroArmazenamentoResponse> listarCentros(Long idEmpresa) {
        return jdbcTemplate.query(SQL_LISTAR_CENTROS, new Object[]{idEmpresa}, centroMapper);
    }

    public CentroArmazenamentoResponse buscarCentroPorId(Long idEmpresa, Long id) {
        List<CentroArmazenamentoResponse> resultados = jdbcTemplate.query(
                SQL_BUSCAR_CENTRO_POR_ID, new Object[]{id, idEmpresa}, centroMapper);
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    public Long cadastrarCentro(Long idEmpresa, CadastrarCentroRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_CENTRO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEmpresa);
            ps.setString(2, request.nome());
            ps.setString(3, request.descricao());
            ps.setString(4, request.tipo());
            ps.setString(5, request.endereco());
            ps.setString(6, request.cep());
            ps.setString(7, request.cidade());
            ps.setString(8, request.uf());
            if (request.idResponsavel() != null) {
                ps.setLong(9, request.idResponsavel());
            } else {
                ps.setNull(9, Types.BIGINT);
            }
            ps.setString(10, request.status() != null ? request.status() : "ATIVO");
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_centro")).longValue();
    }

    public void atualizarCentro(Long idEmpresa, Long id, CadastrarCentroRequest request) {
        jdbcTemplate.update(SQL_ATUALIZAR_CENTRO,
                request.nome(), request.descricao(), request.tipo(), request.endereco(),
                request.cep(), request.cidade(), request.uf(), request.idResponsavel(),
                request.status() != null ? request.status() : "ATIVO", id, idEmpresa);
    }

    public void desativarCentro(Long idEmpresa, Long id) {
        jdbcTemplate.update(SQL_DESATIVAR_CENTRO, id, idEmpresa);
    }

    // ============================================
    // LOCALIZAÇÕES
    // ============================================

    private static final String SQL_LISTAR_LOCALIZACOES_POR_CENTRO = """
            SELECT id_localizacao, id_centro, codigo, descricao, corredor, prateleira,
                   nivel, capacidade_max, status, criado_em
            FROM tb_localizacao
            WHERE id_centro = ? AND id_empresa = ?
            ORDER BY codigo
            """;

    private static final String SQL_CADASTRAR_LOCALIZACAO = """
            INSERT INTO tb_localizacao (id_empresa, id_centro, codigo, descricao, corredor, prateleira, nivel, capacidade_max, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public List<LocalizacaoResponse> listarLocalizacoesPorCentro(Long idEmpresa, Long idCentro) {
        return jdbcTemplate.query(SQL_LISTAR_LOCALIZACOES_POR_CENTRO, new Object[]{idCentro, idEmpresa}, localizacaoMapper);
    }

    public Long cadastrarLocalizacao(Long idEmpresa, Long idCentro, CadastrarLocalizacaoRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_LOCALIZACAO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEmpresa);
            ps.setLong(2, idCentro);
            ps.setString(3, request.codigo());
            ps.setString(4, request.descricao());
            ps.setString(5, request.corredor());
            ps.setString(6, request.prateleira());
            ps.setString(7, request.nivel());
            if (request.capacidadeMax() != null) {
                ps.setInt(8, request.capacidadeMax());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setString(9, request.status() != null ? request.status() : "ATIVO");
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
            WHERE pl.id_produto = ? AND pl.id_empresa = ?
            """;

    private static final String SQL_VINCULAR_PRODUTO_LOCALIZACAO = """
            INSERT INTO tb_produto_localizacao (id_empresa, id_produto, id_localizacao, quantidade)
            VALUES (?, ?, ?, ?)
            """;

    public List<ProdutoLocalizacaoResponse> listarLocalizacoesProduto(Long idEmpresa, Long idProduto) {
        return jdbcTemplate.query(SQL_LISTAR_LOCALIZACOES_PRODUTO, new Object[]{idProduto, idEmpresa}, produtoLocalizacaoMapper);
    }

    public void vincularProdutoLocalizacao(Long idEmpresa, VincularProdutoLocalizacaoRequest request) {
        jdbcTemplate.update(SQL_VINCULAR_PRODUTO_LOCALIZACAO,
                idEmpresa, request.idProduto(), request.idLocalizacao(),
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
            WHERE p.status = 'ATIVO' AND p.id_empresa = ?
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
            WHERE p.id_produto = ? AND p.id_empresa = ?
            """;

    private static final String SQL_CADASTRAR_PRODUTO = """
            INSERT INTO tb_produto (id_empresa, nome, descricao, id_categoria, id_fornecedor, id_centro_padrao,
                                    unidade_medida, preco_custo, preco_venda,
                                    qtd_estoque_minimo, qtd_estoque_maximo, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ATUALIZAR_PRODUTO = """
            UPDATE tb_produto
            SET nome = ?, descricao = ?, id_categoria = ?, id_fornecedor = ?, id_centro_padrao = ?,
                unidade_medida = ?, preco_custo = ?, preco_venda = ?,
                qtd_estoque_minimo = ?, qtd_estoque_maximo = ?, status = ?
            WHERE id_produto = ? AND id_empresa = ?
            """;

    private static final String SQL_DESATIVAR_PRODUTO = """
            UPDATE tb_produto SET status = 'INATIVO' WHERE id_produto = ? AND id_empresa = ?
            """;

    public List<ProdutoResponse> listarProdutos(Long idEmpresa) {
        return jdbcTemplate.query(SQL_LISTAR_PRODUTOS, new Object[]{idEmpresa}, produtoMapper);
    }

    public ProdutoResponse buscarProdutoPorId(Long idEmpresa, Long id) {
        List<ProdutoResponse> resultados = jdbcTemplate.query(
                SQL_BUSCAR_PRODUTO_POR_ID, new Object[]{id, idEmpresa}, produtoMapper);
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    public Long cadastrarProduto(Long idEmpresa, CadastrarProdutoRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_PRODUTO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEmpresa);
            ps.setString(2, request.nome());
            ps.setString(3, request.descricao());
            if (request.idCategoria() != null) {
                ps.setLong(4, request.idCategoria());
            } else {
                ps.setNull(4, Types.BIGINT);
            }
            if (request.idFornecedor() != null) {
                ps.setLong(5, request.idFornecedor());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setLong(6, request.idCentroPadrao());
            ps.setString(7, request.unidadeMedida());
            ps.setBigDecimal(8, request.precoCusto());
            ps.setBigDecimal(9, request.precoVenda());
            ps.setInt(10, request.qtdEstoqueMinimo());
            ps.setInt(11, request.qtdEstoqueMaximo());
            ps.setString(12, request.status() != null ? request.status() : "ATIVO");
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_produto")).longValue();
    }

    public void atualizarProduto(Long idEmpresa, Long id, AtualizarProdutoRequest request) {
        ProdutoResponse atual = buscarProdutoPorId(idEmpresa, id);
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
                id, idEmpresa);
    }

    public void desativarProduto(Long idEmpresa, Long id) {
        jdbcTemplate.update(SQL_DESATIVAR_PRODUTO, id, idEmpresa);
    }

    // ============================================
    // CATEGORIAS
    // ============================================

    private static final String SQL_LISTAR_CATEGORIAS = """
            SELECT id_categoria, nome, descricao, status, criado_em, atualizado_em
            FROM tb_categoria
            WHERE status = 'ATIVO' AND id_empresa = ?
            ORDER BY nome
            """;

    private static final String SQL_BUSCAR_CATEGORIA_POR_ID = """
            SELECT id_categoria, nome, descricao, status, criado_em, atualizado_em
            FROM tb_categoria
            WHERE id_categoria = ? AND id_empresa = ?
            """;

    private static final String SQL_CADASTRAR_CATEGORIA = """
            INSERT INTO tb_categoria (id_empresa, nome, descricao, status)
            VALUES (?, ?, ?, ?)
            """;

    private static final String SQL_ATUALIZAR_CATEGORIA = """
            UPDATE tb_categoria
            SET nome = ?, descricao = ?, status = ?
            WHERE id_categoria = ? AND id_empresa = ?
            """;

    private static final String SQL_DESATIVAR_CATEGORIA = """
            UPDATE tb_categoria SET status = 'INATIVO' WHERE id_categoria = ? AND id_empresa = ?
            """;

    public List<CategoriaResponse> listarCategorias(Long idEmpresa) {
        return jdbcTemplate.query(SQL_LISTAR_CATEGORIAS, new Object[]{idEmpresa}, categoriaMapper);
    }

    public CategoriaResponse buscarCategoriaPorId(Long idEmpresa, Long id) {
        List<CategoriaResponse> resultados = jdbcTemplate.query(
                SQL_BUSCAR_CATEGORIA_POR_ID, new Object[]{id, idEmpresa}, categoriaMapper);
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    public Long cadastrarCategoria(Long idEmpresa, CadastrarCategoriaRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_CATEGORIA, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEmpresa);
            ps.setString(2, request.nome());
            ps.setString(3, request.descricao());
            ps.setString(4, request.status() != null ? request.status() : "ATIVO");
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_categoria")).longValue();
    }

    public void atualizarCategoria(Long idEmpresa, Long id, CadastrarCategoriaRequest request) {
        jdbcTemplate.update(SQL_ATUALIZAR_CATEGORIA,
                request.nome(), request.descricao(),
                request.status() != null ? request.status() : "ATIVO", id, idEmpresa);
    }

    public void desativarCategoria(Long idEmpresa, Long id) {
        jdbcTemplate.update(SQL_DESATIVAR_CATEGORIA, id, idEmpresa);
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
            WHERE 1=1 AND m.id_empresa = ?
            """;

    private static final String SQL_HISTORICO_PRODUTO = """
            SELECT m.id_movimentacao, m.id_produto, p.nome AS produto_nome,
                   m.tipo, m.origem, m.quantidade, m.numero_nf, m.lote,
                   m.validade, m.observacao, m.id_usuario, m.criado_em
            FROM tb_movimentacao_estoque m
            JOIN tb_produto p ON m.id_produto = p.id_produto
            WHERE m.id_produto = ? AND m.id_empresa = ?
            ORDER BY m.criado_em DESC
            """;

    private static final String SQL_REGISTRAR_MOVIMENTACAO = """
            INSERT INTO tb_movimentacao_estoque (id_empresa, id_produto, tipo, origem, quantidade, numero_nf, lote, validade, observacao, id_usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ATUALIZAR_ESTOQUE_ENTRADA = """
            UPDATE tb_produto SET qtd_estoque_atual = qtd_estoque_atual + ? WHERE id_produto = ? AND id_empresa = ?
            """;

    private static final String SQL_ATUALIZAR_ESTOQUE_SAIDA = """
            UPDATE tb_produto SET qtd_estoque_atual = qtd_estoque_atual - ? WHERE id_produto = ? AND id_empresa = ?
            """;

    private static final String SQL_VERIFICAR_ESTOQUE = """
            SELECT qtd_estoque_atual FROM tb_produto WHERE id_produto = ? AND id_empresa = ?
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
            WHERE p.id_produto = ? AND p.status = 'ATIVO' AND p.id_empresa = ?
            """;

    public List<MovimentacaoResponse> listarMovimentacoes(Long idEmpresa, String tipo, LocalDate dataInicio, LocalDate dataFim) {
        StringBuilder sql = new StringBuilder(SQL_LISTAR_MOVIMENTACOES);
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);

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

    public List<MovimentacaoResponse> historicoProduto(Long idEmpresa, Long idProduto) {
        return jdbcTemplate.query(SQL_HISTORICO_PRODUTO, new Object[]{idProduto, idEmpresa}, movimentacaoMapper);
    }

    public Long registrarEntrada(Long idEmpresa, RegistrarEntradaRequest request, Long idUsuario) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_REGISTRAR_MOVIMENTACAO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEmpresa);
            ps.setLong(2, request.idProduto());
            ps.setString(3, "ENTRADA");
            ps.setString(4, request.origem());
            ps.setInt(5, request.quantidade());
            ps.setString(6, request.numeroNf());
            ps.setString(7, request.lote());
            if (request.validade() != null) {
                ps.setDate(8, java.sql.Date.valueOf(request.validade()));
            } else {
                ps.setNull(8, Types.DATE);
            }
            ps.setString(9, request.observacao());
            ps.setLong(10, idUsuario);
            return ps;
        }, keyHolder);

        jdbcTemplate.update(SQL_ATUALIZAR_ESTOQUE_ENTRADA, request.quantidade(), request.idProduto(), idEmpresa);

        return ((Number) keyHolder.getKeyList().getFirst().get("id_movimentacao")).longValue();
    }

    public void registrarSaida(Long idEmpresa, RegistrarSaidaRequest request, Long idUsuario) {
        Integer estoqueAtual = jdbcTemplate.queryForObject(
                SQL_VERIFICAR_ESTOQUE, new Object[]{request.idProduto(), idEmpresa}, Integer.class);

        if (estoqueAtual == null || estoqueAtual < request.quantidade()) {
            throw new keysson.apis.estoque.exception.BusinessRuleException(
                    keysson.apis.estoque.exception.enums.ErrorCode.ERROR_ESTOQUE_INSUFICIENTE);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_REGISTRAR_MOVIMENTACAO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEmpresa);
            ps.setLong(2, request.idProduto());
            ps.setString(3, "SAIDA");
            ps.setString(4, request.origem());
            ps.setInt(5, request.quantidade());
            ps.setNull(6, Types.VARCHAR);
            ps.setNull(7, Types.VARCHAR);
            ps.setNull(8, Types.DATE);
            ps.setString(9, request.observacao());
            ps.setLong(10, idUsuario);
            return ps;
        }, keyHolder);

        jdbcTemplate.update(SQL_ATUALIZAR_ESTOQUE_SAIDA, request.quantidade(), request.idProduto(), idEmpresa);
    }

    public EstoqueDisponivelResponse estoqueDisponivel(Long idEmpresa, Long idProduto) {
        List<EstoqueDisponivelResponse> resultados = jdbcTemplate.query(
                SQL_ESTOQUE_DISPONIVEL, new Object[]{idProduto, idEmpresa},
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
            INSERT INTO tb_inventario (id_empresa, id_produto, qtd_sistema, qtd_fisica, divergencia, status, id_usuario)
            VALUES (?, ?, ?, ?, ?, 'PENDENTE', ?)
            """;

    private static final String SQL_LISTAR_DIVERGENCIAS = """
            SELECT i.id_inventario, i.id_produto, p.nome AS produto_nome,
                   i.qtd_sistema, i.qtd_fisica, i.divergencia, i.status, i.id_usuario,
                   i.observacao, i.criado_em
            FROM tb_inventario i
            JOIN tb_produto p ON i.id_produto = p.id_produto
            WHERE i.status IN ('PENDENTE', 'FINALIZADO') AND i.id_empresa = ?
            ORDER BY i.criado_em DESC
            """;

    private static final String SQL_AJUSTAR_INVENTARIO = """
            UPDATE tb_inventario SET status = 'AJUSTADO' WHERE id_inventario = ? AND id_empresa = ?
            """;

    private static final String SQL_ATUALIZAR_ESTOQUE_POR_INVENTARIO = """
            UPDATE tb_produto SET qtd_estoque_atual = ? WHERE id_produto = ? AND id_empresa = ?
            """;

    private static final String SQL_BUSCAR_INVENTARIO_POR_ID = """
            SELECT i.id_inventario, i.id_produto, p.nome AS produto_nome,
                   i.qtd_sistema, i.qtd_fisica, i.divergencia, i.status, i.id_usuario,
                   i.observacao, i.criado_em
            FROM tb_inventario i
            JOIN tb_produto p ON i.id_produto = p.id_produto
            WHERE i.id_inventario = ? AND i.id_empresa = ?
            """;

    public Long cadastrarInventario(Long idEmpresa, Long idProduto, Long idUsuario) {
        Integer qtdSistemaRaw = jdbcTemplate.queryForObject(
                SQL_VERIFICAR_ESTOQUE, new Object[]{idProduto, idEmpresa}, Integer.class);
        int qtdSistema = qtdSistemaRaw != null ? qtdSistemaRaw : 0;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_CADASTRAR_INVENTARIO, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, idEmpresa);
            ps.setLong(2, idProduto);
            ps.setInt(3, qtdSistema);
            ps.setNull(4, Types.INTEGER);
            ps.setNull(5, Types.INTEGER);
            ps.setLong(6, idUsuario);
            return ps;
        }, keyHolder);
        return ((Number) keyHolder.getKeyList().getFirst().get("id_inventario")).longValue();
    }

    public void registrarContagem(Long idEmpresa, Long idInventario, Integer qtdFisica, String observacao) {
        InventarioResponse inv = buscarInventarioPorId(idEmpresa, idInventario);
        if (inv == null) return;

        int divergencia = Math.abs(qtdFisica - inv.qtdSistema());
        String sql = "UPDATE tb_inventario SET qtd_fisica = ?, divergencia = ?, observacao = ?, status = 'FINALIZADO' WHERE id_inventario = ? AND id_empresa = ?";
        jdbcTemplate.update(sql, qtdFisica, divergencia, observacao, idInventario, idEmpresa);
    }

    public InventarioResponse buscarInventarioPorId(Long idEmpresa, Long id) {
        List<InventarioResponse> resultados = jdbcTemplate.query(
                SQL_BUSCAR_INVENTARIO_POR_ID, new Object[]{id, idEmpresa}, inventarioMapper);
        return resultados.isEmpty() ? null : resultados.getFirst();
    }

    public List<InventarioResponse> listarDivergencias(Long idEmpresa) {
        return jdbcTemplate.query(SQL_LISTAR_DIVERGENCIAS, new Object[]{idEmpresa}, inventarioMapper);
    }

    public void ajustarInventario(Long idEmpresa, Long idInventario) {
        InventarioResponse inv = buscarInventarioPorId(idEmpresa, idInventario);
        if (inv == null) return;

        jdbcTemplate.update(SQL_ATUALIZAR_ESTOQUE_POR_INVENTARIO, inv.qtdFisica(), inv.idProduto(), idEmpresa);
        jdbcTemplate.update(SQL_AJUSTAR_INVENTARIO, idInventario, idEmpresa);
    }

    // ============================================
    // DASHBOARD
    // ============================================

    private static final String SQL_DASHBOARD_TOTAL_ITENS = """
            SELECT COUNT(*) FROM tb_produto WHERE status = 'ATIVO' AND id_empresa = ?
            """;

    private static final String SQL_DASHBOARD_ESTOQUE_BAIXO = """
            SELECT COUNT(*) FROM tb_produto
            WHERE status = 'ATIVO' AND id_empresa = ? AND qtd_estoque_atual > 0 AND qtd_estoque_atual <= qtd_estoque_minimo
            """;

    private static final String SQL_DASHBOARD_ALERTAS_CRITICOS = """
            SELECT COUNT(*) FROM tb_produto
            WHERE status = 'ATIVO' AND id_empresa = ? AND (qtd_estoque_atual = 0 OR qtd_estoque_atual < 0)
            """;

    private static final String SQL_DASHBOARD_VALOR_TOTAL = """
            SELECT COALESCE(SUM(qtd_estoque_atual * preco_custo), 0) FROM tb_produto WHERE status = 'ATIVO' AND id_empresa = ?
            """;

    public DashboardResponse buscarDashboard(Long idEmpresa) {
        Long totalItens = jdbcTemplate.queryForObject(SQL_DASHBOARD_TOTAL_ITENS, new Object[]{idEmpresa}, Long.class);
        Long estoqueBaixo = jdbcTemplate.queryForObject(SQL_DASHBOARD_ESTOQUE_BAIXO, new Object[]{idEmpresa}, Long.class);
        Long alertasCriticos = jdbcTemplate.queryForObject(SQL_DASHBOARD_ALERTAS_CRITICOS, new Object[]{idEmpresa}, Long.class);
        BigDecimal valorTotal = jdbcTemplate.queryForObject(SQL_DASHBOARD_VALOR_TOTAL, new Object[]{idEmpresa}, BigDecimal.class);

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
            WHERE p.status = 'ATIVO' AND p.id_empresa = ?
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
                FROM tb_movimentacao_estoque WHERE tipo = 'ENTRADA' AND id_empresa = ?
                GROUP BY id_produto
            ) entradas ON p.id_produto = entradas.id_produto
            LEFT JOIN (
                SELECT id_produto, SUM(quantidade) AS total
                FROM tb_movimentacao_estoque WHERE tipo = 'SAIDA' AND id_empresa = ?
                GROUP BY id_produto
            ) saidas ON p.id_produto = saidas.id_produto
            WHERE p.status = 'ATIVO' AND p.id_empresa = ?
            ORDER BY giro_estoque DESC
            """;

    public List<RelatorioValorResponse> relatorioValor(Long idEmpresa) {
        return jdbcTemplate.query(SQL_RELATORIO_VALOR, new Object[]{idEmpresa},
                (rs, rowNum) -> new RelatorioValorResponse(
                        rs.getLong("id_categoria"),
                        rs.getString("categoria_nome"),
                        rs.getInt("total_produtos"),
                        rs.getInt("quantidade_total"),
                        rs.getBigDecimal("valor_total")
                ));
    }

    public List<RelatorioGiroResponse> relatorioGiro(Long idEmpresa) {
        return jdbcTemplate.query(SQL_RELATORIO_GIRO, new Object[]{idEmpresa, idEmpresa, idEmpresa},
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
