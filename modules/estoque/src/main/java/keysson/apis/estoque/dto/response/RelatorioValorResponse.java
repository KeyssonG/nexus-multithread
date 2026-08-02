package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RelatorioValorResponse(
        Long idCategoria,
        String categoriaNome,
        Integer totalProdutos,
        Integer quantidadeTotal,
        BigDecimal valorTotal
) {
}
