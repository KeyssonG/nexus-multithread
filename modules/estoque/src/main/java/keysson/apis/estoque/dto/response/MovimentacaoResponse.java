package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MovimentacaoResponse(
        Long idMovimentacao,
        Long idProduto,
        String produtoNome,
        String tipo,
        String origem,
        Integer quantidade,
        String numeroNf,
        String lote,
        LocalDate validade,
        String observacao,
        Long idUsuario,
        LocalDateTime criadoEm
) {
}
