package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CheckBookCreateDTO {
    @NotNull(message = "CheckBook type is required")
    private CheckBookType checkBookType;

    @NotNull(message = "CheckBook leave type is required")
    private CheckBookLeaveType checkBookLeaveType;

    private LocalDateTime receivedDate;
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
