package cbo.risk.sms.dtos;

import cbo.risk.sms.dtos.BaseStockDTO;
import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
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
public class CheckBookDTO extends BaseStockDTO {
    @NotNull(message = "CheckBook type is required")
    private CheckBookType checkBookType;

    @NotNull(message = "CheckBook leave type is required")
    private CheckBookLeaveType checkBookLeaveType;
    private String startSerialNumber;
    private String endSerialNumber;

    private Long bookParentId;
    private String issuedTo;
    private String issuedBy;
    private String returnedBy;
}

