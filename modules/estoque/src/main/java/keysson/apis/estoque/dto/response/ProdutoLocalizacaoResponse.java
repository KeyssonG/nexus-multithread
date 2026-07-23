package keysson.apis.estoque.dto.response;

import java.time.LocalDateTime;

public record ProdutoLocalizacaoResponse(
        Long idProdutoLocalizacao,
        Long idProduto,
        Long idLocalizacao,
        String codigoLocalizacao,
        Integer quantidade,
        LocalDateTime criadoEm
) {
}
