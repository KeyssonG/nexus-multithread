package keysson.apis.estoque.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CadastrarLocalizacaoRequest(
        @NotBlank(message = "Código é obrigatório")
        String codigo,

        String descricao,

        String corredor,

        String prateleira,

        String nivel,

        Integer capacidadeMax,

        String status
) {
}
