package keysson.apis.estoque.mapper;

import keysson.apis.estoque.dto.response.ProdutoLocalizacaoResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ProdutoLocalizacaoRowMapper implements RowMapper<ProdutoLocalizacaoResponse> {

    @Override
    public ProdutoLocalizacaoResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProdutoLocalizacaoResponse(
                rs.getLong("id_produto_localizacao"),
                rs.getLong("id_produto"),
                rs.getLong("id_localizacao"),
                rs.getString("codigo_localizacao"),
                rs.getInt("quantidade"),
                rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null
        );
    }
}
