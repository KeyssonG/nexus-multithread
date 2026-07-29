package keysson.apis.estoque.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VincularProdutoLocalizacaoRequest(
        @NotNull(message = "ID do produto é obrigatório")
        Long idProduto,

        @NotNull(message = "ID da localização é obrigatório")
        Long idLocalizacao,

        Integer quantidade
) {
}
