package keysson.apis.estoque.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CadastrarCentroRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres")
        String nome,

        String descricao,

        @NotBlank(message = "Tipo é obrigatório")
        String tipo,

        @NotBlank(message = "Endereço é obrigatório")
        String endereco,

        @NotBlank(message = "CEP é obrigatório")
        String cep,

        @NotBlank(message = "Cidade é obrigatória")
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
        String cidade,

        @NotBlank(message = "UF é obrigatória")
        String uf,

        Long idResponsavel,

        String status
) {
}
