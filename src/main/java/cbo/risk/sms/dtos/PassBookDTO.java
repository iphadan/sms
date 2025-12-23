package cbo.risk.sms.dtos;

import cbo.risk.sms.dtos.BaseStockDTO;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class PassBookDTO extends BaseStockDTO {
    @NotNull(message = "PassBook type is required")
    private PassBookType passBookType;

    @NotNull(message = "PassBook category is required")
    private PassBookCategory passBookCategory;
}

