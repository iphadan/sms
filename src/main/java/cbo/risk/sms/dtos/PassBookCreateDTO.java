package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class PassBookCreateDTO {
    @NotNull(message = "PassBook type is required")
    private PassBookType passBookType;

    @NotNull(message = "PassBook category is required")
    private PassBookCategory passBookCategory;

    private LocalDateTime receivedDate;

    @NotNull(message = "Number of pads is required")
    private int numOfPad;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Sub-process ID is required")
    private String subProcessId;

    @NotBlank(message = "Process ID is required")
    private String processId;

    @NotBlank(message = "Creator is required")
    private String createdBy;
}
