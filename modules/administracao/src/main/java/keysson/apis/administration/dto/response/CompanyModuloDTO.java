package keysson.apis.administration.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanyModuloDTO {
    private int moduloId;
    private String moduloName;
    private int status;
    private String statusDescription;
}
