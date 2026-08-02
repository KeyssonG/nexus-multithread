package keysson.apis.estoque.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DashboardResponse(
        Long totalItens,
        Long estoqueBaixo,
        Long alertasCriticos,
        java.math.BigDecimal valorTotalEstoque
) {
}
