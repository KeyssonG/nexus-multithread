package keysson.apis.estoque.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

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
