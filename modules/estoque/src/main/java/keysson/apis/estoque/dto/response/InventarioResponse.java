package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record InventarioResponse(
        Long idInventario,
        Long idProduto,
        String produtoNome,
        Integer qtdSistema,
        Integer qtdFisica,
        Integer divergencia,
        String status,
        Long idUsuario,
        String observacao,
        LocalDateTime criadoEm
) {
}
