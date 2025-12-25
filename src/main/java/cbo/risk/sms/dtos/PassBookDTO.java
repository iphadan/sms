package cbo.risk.sms.dtos;

import cbo.risk.sms.dtos.BaseStockDTO;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
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
public class PassBookDTO extends BaseStockDTO {
    @NotNull(message = "PassBook type is required")
    private PassBookType passBookType;

    @NotNull(message = "PassBook category is required")
    private PassBookCategory passBookCategory;

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

