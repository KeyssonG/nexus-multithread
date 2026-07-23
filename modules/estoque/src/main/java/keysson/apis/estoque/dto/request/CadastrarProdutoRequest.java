package keysson.apis.estoque.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CadastrarProdutoRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String nome,

        String descricao,

        @NotNull(message = "Categoria é obrigatória")
        Long idCategoria,

        Long idFornecedor,

        @NotNull(message = "Centro de armazenamento padrão é obrigatório")
        Long idCentroPadrao,

        @NotBlank(message = "Unidade de medida é obrigatória")
        String unidadeMedida,

        @NotNull(message = "Preço de custo é obrigatório")
        @Positive(message = "Preço de custo deve ser maior que zero")
        BigDecimal precoCusto,

        BigDecimal precoVenda,

        @NotNull(message = "Estoque mínimo é obrigatório")
        Integer qtdEstoqueMinimo,

        @NotNull(message = "Estoque máximo é obrigatório")
        Integer qtdEstoqueMaximo,

        String status
) {
}
