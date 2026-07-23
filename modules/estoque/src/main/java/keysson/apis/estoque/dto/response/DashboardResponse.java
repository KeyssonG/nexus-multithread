package keysson.apis.estoque.dto.response;

public record DashboardResponse(
        Long totalItens,
        Long estoqueBaixo,
        Long alertasCriticos,
        java.math.BigDecimal valorTotalEstoque
) {
}
