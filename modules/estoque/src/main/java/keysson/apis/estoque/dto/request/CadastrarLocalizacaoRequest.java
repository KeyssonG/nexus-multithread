package keysson.apis.estoque.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
