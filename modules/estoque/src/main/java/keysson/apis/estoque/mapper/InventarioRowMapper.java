package keysson.apis.estoque.mapper;

import keysson.apis.estoque.dto.response.InventarioResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class InventarioRowMapper implements RowMapper<InventarioResponse> {

    @Override
    public InventarioResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        int qtdFisica = rs.getInt("qtd_fisica");
        Integer qtdFisicaValue = rs.wasNull() ? null : qtdFisica;
        int divergencia = rs.getInt("divergencia");
        Integer divergenciaValue = rs.wasNull() ? null : divergencia;
        return new InventarioResponse(
                rs.getLong("id_inventario"),
                rs.getLong("id_produto"),
                rs.getString("produto_nome"),
                rs.getInt("qtd_sistema"),
                qtdFisicaValue,
                divergenciaValue,
                rs.getString("status"),
                rs.getLong("id_usuario"),
                rs.getString("observacao"),
                rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null
        );
    }
}
