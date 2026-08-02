package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LocalizacaoResponse(
        Long idLocalizacao,
        Long idCentro,
        String codigo,
        String descricao,
        String corredor,
        String prateleira,
        String nivel,
        Integer capacidadeMax,
        String status,
        LocalDateTime criadoEm
) {
}
