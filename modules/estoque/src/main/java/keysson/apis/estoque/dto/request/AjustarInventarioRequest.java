package keysson.apis.estoque.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AjustarInventarioRequest(
        @NotNull(message = "ID do inventário é obrigatório")
        Long idInventario,

        String justificativa
) {
}
