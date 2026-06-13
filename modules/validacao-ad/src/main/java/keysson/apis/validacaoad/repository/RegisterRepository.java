package keysson.apis.validacaoad.repository;

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import keysson.apis.validacaoad.dto.response.FuncionarioRegistroResultado;

@Repository("validacaoADRegisterRepository")
public class RegisterRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    public RegisterRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String CHECK_EXISTS_CPF= """
        SELECT COUNT(*) 
        FROM funcionarios 
        WHERE cpf = ?
        """;

    private static final String CHECK_EXISTS_USERNAME= """
        SELECT COUNT(*)
        FROM USERS
        WHERE username  = ? and company_id = 0
        """;

    private static final String CHECK_EXISTS_NUMERO_CONTA = """
             SELECT COUNT(*)
             FROM users
             WHERE conta_matricula = ?
        """;

    private static final String CHECK_EXISTS_EMAIL = """
             SELECT COUNT(*)
             FROM contatos
             WHERE email = ?
        """;

    public boolean existsByCpf(String cpf) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_CPF, Long.class, cpf);
        return count != null && count > 0;
    }

    public boolean existsByUsername(String name) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_USERNAME, Long.class, name);
        return count != null && count > 0;
    }

    public FuncionarioRegistroResultado save(String nome, Date dataNascimento, String email, String cpf, String sexo, String password,
                                             String username, String departamento, int numeroConta, String telefone) {

        String sql = "CALL proc_registrar_funcionario(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Map<String, Object> result = jdbcTemplate.call(connection -> {
            CallableStatement cs = connection.prepareCall(sql);


            cs.setString(1, nome);
            cs.setDate(2, new java.sql.Date(dataNascimento.getTime()));
            cs.setString(3, departamento);
            cs.setString(4, email);
            cs.setString(5, cpf);
            cs.setString(6, sexo);
            cs.setString(7, username);
            cs.setString(8, password);
            cs.setInt(9, numeroConta);
            cs.setString(10, telefone);
            cs.registerOutParameter(11, Types.INTEGER);
            cs.registerOutParameter(12, Types.INTEGER);

            return cs;
        }, Arrays.asList(
                new SqlParameter("p_nome", Types.VARCHAR),
                new SqlParameter("p_data_nascimento", Types.DATE),
                new SqlParameter("p_departamento", Types.VARCHAR),
                new SqlParameter("p_email", Types.VARCHAR),
                new SqlParameter("p_cpf", Types.VARCHAR),
                new SqlParameter("p_sexo", Types.VARCHAR),
                new SqlParameter("p_username", Types.VARCHAR),
                new SqlParameter("p_password", Types.VARCHAR),
                new SqlParameter("p_numero_conta", Types.INTEGER),
                new SqlParameter("p_telefone", Types.VARCHAR),
                new SqlOutParameter("out_result", Types.INTEGER),
                new SqlOutParameter("out_user_id", Types.INTEGER)
        ));

        System.out.println("Map result: " + result);

        Integer resultCode = (Integer) result.get("out_result");
        Integer idFuncionario = (Integer) result.get("out_user_id");

        return new FuncionarioRegistroResultado(resultCode, idFuncionario);
    }


    public boolean existsByNumeroConta(int numeroConta) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_NUMERO_CONTA, Long.class, numeroConta);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        Long count = jdbcTemplate.queryForObject(CHECK_EXISTS_EMAIL, Long.class, email);
        return count != null && count > 0;
    }
}
