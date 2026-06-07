package keysson.apis.administration.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModuloResponseDTO {
    private Integer userId;
    private String userName;
    private String department;
    private Integer moduloId;
    private String moduloName;
}
