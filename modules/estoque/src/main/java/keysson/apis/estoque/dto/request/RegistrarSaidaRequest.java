package keysson.apis.estoque.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
