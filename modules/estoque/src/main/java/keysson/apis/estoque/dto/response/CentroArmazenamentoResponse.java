package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CentroArmazenamentoResponse(
        Long idCentro,
        String nome,
        String descricao,
        String tipo,
        String endereco,
        String cep,
        String cidade,
        String uf,
        Long idResponsavel,
        String status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
