package keysson.apis.validacaoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensagensPendentes {
    private int idFuncionario;
    private String name;
    private String email;
    private String cpf;
    private String username;
    private int status;
}
