package keysson.apis.estoque.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
