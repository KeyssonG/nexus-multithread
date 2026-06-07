package keysson.apis.administration.dto.response;

import lombok.Data;

@Data
public class CompanyStatusDTO {
    private int pendente;
    private int ativo;
    private int rejeitado;

}