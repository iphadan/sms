package cbo.risk.sms.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Getter
@Setter

public class IssueRequestDTO {
    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Issued to is required")
    private String issuedTo;

    @NotBlank(message = "Issued by is required")
    private String issuedBy;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    private String createdBy;

    private String createdById;



    private String lastUpdatedBy;

    private String lastUpdatedById;
}
