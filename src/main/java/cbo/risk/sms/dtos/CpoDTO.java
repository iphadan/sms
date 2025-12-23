package cbo.risk.sms.dtos;

import cbo.risk.sms.dtos.BaseStockDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CpoDTO extends BaseStockDTO {
    // Additional CPO-specific fields can be added here
}

