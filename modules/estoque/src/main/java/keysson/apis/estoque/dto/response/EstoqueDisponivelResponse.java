package keysson.apis.estoque.dto.response;

import java.math.BigDecimal;

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
