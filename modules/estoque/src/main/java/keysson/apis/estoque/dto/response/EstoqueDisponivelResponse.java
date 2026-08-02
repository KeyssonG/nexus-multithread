package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record EstoqueDisponivelResponse(
        Long idProduto,
        String produtoNome,
        Integer qtdEstoqueAtual,
        Integer qtdEstoqueMinimo,
        Integer qtdEstoqueMaximo,
        String statusEstoque,
        BigDecimal valorTotal
) {
}
