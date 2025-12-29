package cbo.risk.sms.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReturnRequestDTO {
    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Returned by is required")
    private String returnedBy;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

}
