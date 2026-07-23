package keysson.apis.estoque.dto.response;

import java.time.LocalDateTime;

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
