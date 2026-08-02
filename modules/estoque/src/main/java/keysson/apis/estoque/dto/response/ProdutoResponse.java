package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProdutoResponse(
        Long idProduto,
        String nome,
        String descricao,
        Long idCategoria,
        String categoriaNome,
        Long idFornecedor,
        Long idCentroPadrao,
        String centroPadraoNome,
        String unidadeMedida,
        BigDecimal precoCusto,
        BigDecimal precoVenda,
        Integer qtdEstoqueAtual,
        Integer qtdEstoqueMinimo,
        Integer qtdEstoqueMaximo,
        String status,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
