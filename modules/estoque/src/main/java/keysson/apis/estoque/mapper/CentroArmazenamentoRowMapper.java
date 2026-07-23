package keysson.apis.estoque.mapper;

import keysson.apis.estoque.dto.response.CentroArmazenamentoResponse;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CentroArmazenamentoRowMapper implements RowMapper<CentroArmazenamentoResponse> {

    @Override
    public CentroArmazenamentoResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CentroArmazenamentoResponse(
                rs.getLong("id_centro"),
                rs.getString("nome"),
                rs.getString("descricao"),
                rs.getString("tipo"),
                rs.getString("endereco"),
                rs.getString("cep"),
                rs.getString("cidade"),
                rs.getString("uf"),
                rs.getObject("id_responsavel") != null ? rs.getLong("id_responsavel") : null,
                rs.getString("status"),
                rs.getTimestamp("criado_em") != null ? rs.getTimestamp("criado_em").toLocalDateTime() : null,
                rs.getTimestamp("atualizado_em") != null ? rs.getTimestamp("atualizado_em").toLocalDateTime() : null
        );
    }
}
