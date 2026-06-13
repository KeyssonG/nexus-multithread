package keysson.apis.validacaoad.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import keysson.apis.validacaoad.dto.request.RequestRegister;
import keysson.apis.validacaoad.exception.BusinessRuleException;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.SQLException;

public interface RegisterController {

    @PostMapping("/cadastrar/funcionario-multithread")
    @Operation(
            summary = "Cadastrar um novo funcionário.",
            description = "Endpoint para cadastrar um novo funcionário MultiThread.",
            requestBody = @RequestBody(
                    description = "Dados da novo funcionário.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RequestRegister.class)
                    )
            )
    )
    public void register(@RequestBody RequestRegister requestRegister)
            throws BusinessRuleException, SQLException;
}
