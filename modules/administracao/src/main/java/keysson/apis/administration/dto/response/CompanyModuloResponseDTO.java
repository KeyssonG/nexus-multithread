package keysson.apis.administration.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanyModuloResponseDTO {
    private int id;
    private int companyId;
    private String companyName;
    private int moduloId;
    private String moduloName;
    private int status;
    private String statusDescription;
}
