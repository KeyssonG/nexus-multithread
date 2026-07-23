package keysson.apis.estoque.dto.request;

import jakarta.validation.constraints.NotNull;

public record AjustarInventarioRequest(
        @NotNull(message = "ID do inventário é obrigatório")
        Long idInventario,

        String justificativa
) {
}
