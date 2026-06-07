package keysson.apis.administration.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PendingCompanyDTO {

    private int id;
    private String cnpj;
    private int status;
    private String description;
    private String name;
    private int accountNumber;
}
