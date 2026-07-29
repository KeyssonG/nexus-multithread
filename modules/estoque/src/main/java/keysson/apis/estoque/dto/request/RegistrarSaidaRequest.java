package keysson.apis.estoque.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RegistrarSaidaRequest(
        @NotNull(message = "ID do produto é obrigatório")
        Long idProduto,

        @NotBlank(message = "Origem é obrigatória")
        String origem,

        @NotNull(message = "Quantidade é obrigatória")
        @Positive(message = "Quantidade deve ser maior que zero")
        Integer quantidade,

        String observacao
) {
}
