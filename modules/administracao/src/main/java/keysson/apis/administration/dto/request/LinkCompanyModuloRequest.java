package keysson.apis.administration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkCompanyModuloRequest {
    private int companyId;
    private int moduloId;
    private int status;
}
