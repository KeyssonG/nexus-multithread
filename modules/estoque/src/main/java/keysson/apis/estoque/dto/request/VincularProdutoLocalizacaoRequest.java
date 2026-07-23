package keysson.apis.estoque.dto.request;

import jakarta.validation.constraints.NotNull;

public record VincularProdutoLocalizacaoRequest(
        @NotNull(message = "ID do produto é obrigatório")
        Long idProduto,

        @NotNull(message = "ID da localização é obrigatório")
        Long idLocalizacao,

        Integer quantidade
) {
}
