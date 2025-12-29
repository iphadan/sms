package cbo.risk.sms.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class RequestCreateDTO {
    private String serialNum;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Sub-process ID is required")
    private String subProcessId;

    @NotBlank(message = "Process ID is required")
    private String processId;

    private LocalDateTime receivedDate;
    private LocalDateTime issuedDate;
    private String issuedBy;
    private String receivedBy;
    private String issuedById;
    private String receivedById;

    private String createdById;
    private String lastUpdatedById;
}
