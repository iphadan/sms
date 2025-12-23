package cbo.risk.sms.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RequestCreateDTO {
    private String serialNum;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Sub-process ID is required")
    private String subProcessId;

    @NotBlank(message = "Process ID is required")
    private String processId;

    @NotBlank(message = "Creator is required")
    private String createdBy;
}
