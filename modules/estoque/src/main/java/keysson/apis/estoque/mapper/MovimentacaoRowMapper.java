package keysson.apis.estoque.mapper;

import keysson.apis.estoque.dto.response.MovimentacaoResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MovimentacaoRowMapper implements RowMapper<MovimentacaoResponse> {

    @Override
    public MovimentacaoResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new MovimentacaoResponse(
                rs.getLong("id_movimentacao"),
                rs.getLong("id_produto"),
                rs.getString("produto_nome"),
                rs.getString("tipo"),
                rs.getString("origem"),
                rs.getInt("quantidade"),
                rs.getString("numero_nf"),
                rs.getString("lote"),
                rs.getDate("validade") != null ? rs.getDate("validade").toLocalDate() : null,
                rs.getString("observacao"),
                rs.getLong("id_usuario"),
                rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null
        );
    }
}
