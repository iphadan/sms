package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class CheckBookCreateDTO {
    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotNull(message = "CheckBook type is required")
    private CheckBookType checkBookType;

    @NotNull(message = "CheckBook leave type is required")
    private CheckBookLeaveType checkBookLeaveType;

    private LocalDateTime receivedDate;

    @NotNull(message = "Number of pads is required")
    private int numOfPad;

    private Long bookParentId; // Optional parent batch reference

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Sub-process ID is required")
    private String subProcessId;

    @NotBlank(message = "Process ID is required")
    private String processId;

    @NotBlank(message = "Creator is required")
    private String createdBy;
}
