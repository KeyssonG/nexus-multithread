package keysson.apis.estoque.dto.response;

import java.time.LocalDateTime;

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
