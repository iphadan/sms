package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.CheckBookType;
import cbo.risk.sms.enums.PassBookCategory;
import cbo.risk.sms.enums.PassBookType;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class RequestPassBookDTO  {
    private Long id;
    private String serialNum;
    private String branchId;
    private String subProcessId;
    private String processId;
    private String createdBy;
    private PassBookType passBookType;
    private PassBookCategory passBookCategory;
    private String lastUpdatedBy;
    private LocalDateTime createdTimestamp;
    private LocalDateTime modifiedTimestamp;
    private LocalDateTime receivedDate;
    private LocalDateTime issuedDate;
    private Long passBookId;


    @NotBlank
    @Column(name ="ACCOUNT_NUMBER")
    private String accountNumber;

    private CheckBookType checkBookType;
    private int checkBookLeaveType;

}
