package keysson.apis.administration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortalUnlinkUserModuloRequest {
    private int userId;
    private int moduloId;
    private int companyId;
}
