package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Data
public class BatchRegistrationDTO {

    @NotNull(message = "Book type is required")
    private BookType bookType; // "CHECKBOOK", "CPO", "PASSBOOK"

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

    public BookType getBookType() {
        return bookType;
    }

    public void setBookType(BookType bookType) {
        this.bookType = bookType;
    }

    public String getStartSerial() {
        return startSerial;
    }

    public void setStartSerial(String startSerial) {
        this.startSerial = startSerial;
    }

    public String getEndSerial() {
        return endSerial;
    }

    public void setEndSerial(String endSerial) {
        this.endSerial = endSerial;
    }

    public CheckBookType getCheckBookType() {
        return checkBookType;
    }

    public void setCheckBookType(CheckBookType checkBookType) {
        this.checkBookType = checkBookType;
    }

    public CheckBookLeaveType getCheckBookLeaveType() {
        return checkBookLeaveType;
    }

    public void setCheckBookLeaveType(CheckBookLeaveType checkBookLeaveType) {
        this.checkBookLeaveType = checkBookLeaveType;
    }

    public PassBookType getPassBookType() {
        return passBookType;
    }

    public void setPassBookType(PassBookType passBookType) {
        this.passBookType = passBookType;
    }

    public PassBookCategory getPassBookCategory() {
        return passBookCategory;
    }

    public void setPassBookCategory(PassBookCategory passBookCategory) {
        this.passBookCategory = passBookCategory;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getSubProcessId() {
        return subProcessId;
    }

    public void setSubProcessId(String subProcessId) {
        this.subProcessId = subProcessId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}

