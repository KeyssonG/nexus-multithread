package keysson.apis.administration.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import keysson.apis.administration.dto.response.DepartmentResponse;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class DepartmentsRowMapper implements RowMapper<DepartmentResponse> {
    @Override
    public DepartmentResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        String nomeDepartamento = rs.getString("DEPARTAMENTO");
        int idDepartamento = rs.getInt("ID");
        return new DepartmentResponse(nomeDepartamento, idDepartamento);
    }
}
