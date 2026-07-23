package keysson.apis.estoque.mapper;

import keysson.apis.estoque.dto.response.CategoriaResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CategoriaRowMapper implements RowMapper<CategoriaResponse> {

    @Override
    public CategoriaResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CategoriaResponse(
                rs.getLong("id_categoria"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null
        );
    }
}
