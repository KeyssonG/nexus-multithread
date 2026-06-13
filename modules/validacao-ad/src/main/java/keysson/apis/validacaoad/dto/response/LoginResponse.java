package keysson.apis.validacaoad.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Date expiresAt;
    private Boolean primeiroAcesso;
}
