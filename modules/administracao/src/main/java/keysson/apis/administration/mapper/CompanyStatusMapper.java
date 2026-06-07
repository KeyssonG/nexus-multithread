package keysson.apis.administration.mapper;

import keysson.apis.administration.dto.response.CompanyStatusDTO;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;


@Component
public class CompanyStatusMapper implements RowMapper<CompanyStatusDTO> {

    @Override
    public CompanyStatusDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        CompanyStatusDTO empresasStatusDTO = new CompanyStatusDTO();
        empresasStatusDTO.setPendente(rs.getInt("pendente"));
        empresasStatusDTO.setAtivo(rs.getInt("ativo"));
        empresasStatusDTO.setRejeitado(rs.getInt("rejeitado"));

        return empresasStatusDTO;
    }
}
