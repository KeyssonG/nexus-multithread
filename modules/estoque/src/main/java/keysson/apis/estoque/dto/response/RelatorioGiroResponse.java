package keysson.apis.estoque.dto.response;

import java.math.BigDecimal;

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
