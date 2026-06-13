package keysson.apis.validacaoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetEvent {
    
    private String email;
    private String token;
    private String username;
}
