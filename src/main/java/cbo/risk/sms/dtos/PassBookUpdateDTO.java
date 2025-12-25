package cbo.risk.sms.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class PassBookUpdateDTO {
    private LocalDateTime issuedDate;
    private LocalDateTime returnedDate;
    private int numOfPad;

    @NotBlank(message = "Updater is required")
    private String lastUpdatedBy;
}
