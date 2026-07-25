package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CategoriaResponse(
        Long idCategoria,
        String nome,
        String descricao,
        String status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
