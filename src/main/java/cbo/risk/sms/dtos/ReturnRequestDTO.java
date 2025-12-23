package cbo.risk.sms.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;


public class ReturnRequestDTO {
    @NotBlank(message = "Serial number is required")
    private String serialNumber;

    @NotBlank(message = "Returned by is required")
    private String returnedBy;

    @NotBlank(message = "Branch ID is required")
    private String branchId;

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getReturnedBy() {
        return returnedBy;
    }

    public void setReturnedBy(String returnedBy) {
        this.returnedBy = returnedBy;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
}
