package cbo.risk.sms.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class CpoUpdateDTO {
    private LocalDateTime issuedDate;
    private LocalDateTime returnedDate;
    private int numOfPad;

    @NotBlank(message = "Updater is required")
    private String lastUpdatedBy;
}
