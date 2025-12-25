package cbo.risk.sms.dtos;

import cbo.risk.sms.dtos.BaseStockDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CpoDTO extends BaseStockDTO {
    private String serialNumber;
    private Long bookParentId;
    private String issuedTo;
    private String issuedBy;
    private String returnedBy;

    // Parent stats for convenience
    private Integer parentNumOfPad;
    private Integer parentUsed;
    private Integer parentAvailable;
}

