package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProdutoLocalizacaoResponse(
        Long idProdutoLocalizacao,
        Long idProduto,
        Long idLocalizacao,
        String codigoLocalizacao,
        Integer quantidade,
        LocalDateTime criadoEm
) {
}
