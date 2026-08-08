package keysson.apis.estoque.mapper;

import keysson.apis.estoque.dto.response.LocalizacaoResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class LocalizacaoRowMapper implements RowMapper<LocalizacaoResponse> {

    @Override
    public LocalizacaoResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new LocalizacaoResponse(
                rs.getLong("id_localizacao"),
                rs.getLong("id_centro"),
                rs.getString("codigo"),
                rs.getString("descricao"),
                rs.getString("corredor"),
                rs.getString("prateleira"),
                rs.getString("nivel"),
                rs.getObject("capacidade_max") != null ? rs.getInt("capacidade_max") : null,
                rs.getString("status"),
                rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null,
                rs.getObject("id_produto_localizacao") != null ? rs.getLong("id_produto_localizacao") : null,
                rs.getObject("id_produto") != null ? rs.getLong("id_produto") : null,
                rs.getString("produto_nome"),
                rs.getObject("quantidade") != null ? rs.getInt("quantidade") : null
        );
    }
}
