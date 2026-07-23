package keysson.apis.estoque.dto.request;

import jakarta.validation.constraints.NotNull;

public record RegistrarContagemRequest(
        @NotNull(message = "Quantidade física é obrigatória")
        Integer qtdFisica,

        String observacao
) {
}
