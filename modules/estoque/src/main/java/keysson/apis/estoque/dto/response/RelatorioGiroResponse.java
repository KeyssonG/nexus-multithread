package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record RelatorioGiroResponse(
        Long idProduto,
        String produtoNome,
        String categoriaNome,
        Integer qtdEstoqueAtual,
        Integer totalEntradas,
        Integer totalSaidas,
        BigDecimal giroEstoque
) {
}
