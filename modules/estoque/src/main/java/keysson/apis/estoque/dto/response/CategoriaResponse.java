package keysson.apis.estoque.dto.response;

import java.time.LocalDateTime;

public record CategoriaResponse(
        Long idCategoria,
        String nome,
        String descricao,
        LocalDateTime criadoEm
) {
}
