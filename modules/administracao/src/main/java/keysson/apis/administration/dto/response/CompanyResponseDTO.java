package keysson.apis.administration.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanyResponseDTO {
    private int id;
    private String name;
}
