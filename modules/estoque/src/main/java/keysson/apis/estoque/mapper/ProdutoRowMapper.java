package keysson.apis.estoque.mapper;

import keysson.apis.estoque.dto.response.ProdutoResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ProdutoRowMapper implements RowMapper<ProdutoResponse> {

    @Override
    public ProdutoResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProdutoResponse(
                rs.getLong("id_produto"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getLong("id_categoria"),
                rs.getString("categoria_nome"),
                rs.getObject("id_fornecedor") != null ? rs.getLong("id_fornecedor") : null,
                rs.getLong("id_centro_padrao"),
                rs.getString("centro_padrao_nome"),
                rs.getString("unidade_medida"),
                rs.getBigDecimal("preco_custo"),
                rs.getBigDecimal("preco_venda"),
                rs.getInt("qtd_estoque_atual"),
                rs.getInt("qtd_estoque_minimo"),
                rs.getInt("qtd_estoque_maximo"),
                rs.getString("status"),
                rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null,
                rs.getTimestamp("atualizado_em") != null ? rs.getTimestamp("atualizado_em").toLocalDateTime() : null
        );
    }
}
