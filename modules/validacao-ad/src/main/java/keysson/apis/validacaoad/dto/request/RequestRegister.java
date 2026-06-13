package keysson.apis.validacaoad.dto.request;


import java.util.Date;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestRegister {

    @NotBlank(message = "Nome do funcionário obrigatório.")
    private String nome;

    @NotBlank(message = "Data de Nascimento obrigatório.")
    private Date dataNascimento;

    @NotBlank(message = "E-mail Corporativo obrigatório.")
    private String email;

    @NotBlank(message = "O CPF é obrigatório, somente números")
    @Size(max = 11, message = "O CPF deve ter no máximo 11 caracteres")
    private String cpf;

    @NotBlank(message = "Sexo do funcionário obrigatório.")
    private String sexo;

    @NotBlank(message = "O nome do usuário deve ser preenchido")
    private String username;

    @NotBlank(message = "Departamento que o funcionário pertence")
    private String departamento;

    @NotBlank(message = "O telefone do funcionário deve ser preenchido")
    private String telefone;
}
