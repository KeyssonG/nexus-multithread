package keysson.apis.estoque.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AtualizarProdutoRequest(
        String nome,

        String descricao,

        Long idCategoria,

        Long idFornecedor,

        Long idCentroPadrao,

        String unidadeMedida,

        @Positive(message = "Preço de custo deve ser maior que zero")
        BigDecimal precoCusto,

        BigDecimal precoVenda,

        Integer qtdEstoqueMinimo,

        Integer qtdEstoqueMaximo,

        String status
) {
}
