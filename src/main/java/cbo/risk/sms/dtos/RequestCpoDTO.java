package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.CheckBookType;
import lombok.Data;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class RequestCpoDTO  {
    private Long id;
    private String serialNumber;
    private String branchId;
    private String subProcessId;
    private String processId;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime createdTimestamp;
    private LocalDateTime modifiedTimestamp;
    private LocalDateTime receivedDate;
    private LocalDateTime issuedDate;
    private String issuedBy;
    private String receivedBy;
    private String issuedById;
    private String receivedById;

    private String createdById;
    private String lastUpdatedById;
    private LocalDateTime returnedDate;
    private Long cpoId;

    private String startSerialNumber;
    @NotBlank
    @Column(name ="ACCOUNT_NUMBER")
    private String accountNumber;

}
