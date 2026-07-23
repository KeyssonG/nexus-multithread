package keysson.apis.estoque.dto.response;

import java.time.LocalDateTime;

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
