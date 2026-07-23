package keysson.apis.estoque.dto.response;

import java.math.BigDecimal;

public record RelatorioValorResponse(
        Long idCategoria,
        String categoriaNome,
        Integer totalProdutos,
        Integer quantidadeTotal,
        BigDecimal valorTotal
) {
}
