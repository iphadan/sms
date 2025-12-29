package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Data
public class BatchRegistrationDTO {

    @NotNull(message = "Parent Book type is required")
    @Enumerated(EnumType.STRING)
    private ParentBookType parentBookType; // "CHECKBOOK", "CPO", "PASSBOOK"
    @NotBlank(message = " Book Type is required")
    private String bookType; // "CHECKBOOK", "CPO", "PASSBOOK"

    @NotBlank(message = "Starting serial is required")
    private String startSerial;

    @NotBlank(message = "Ending serial is required")
    private String endSerial;
    @NotNull(message = "Number of Pads is required")
    private Integer numOfPad;

    // For CheckBook
    private CheckBookType checkBookType;
    private CheckBookLeaveType checkBookLeaveType;

    // For PassBook
    private PassBookType passBookType;
    private PassBookCategory passBookCategory;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    @NotBlank(message = "Sub-process ID is required")
    private String subProcessId;

    @NotBlank(message = "Process ID is required")
    private String processId;

    @NotBlank(message = "Creator is required")
    private String createdBy;

    private String createdById;
    private String lastUpdatedBy;

    private String lastUpdatedById;




}

