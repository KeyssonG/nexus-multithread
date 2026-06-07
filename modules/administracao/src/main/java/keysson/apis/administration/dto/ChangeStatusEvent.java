package keysson.apis.administration.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatusEvent {

    private int numeroConta;
    private int newStatus;
}
