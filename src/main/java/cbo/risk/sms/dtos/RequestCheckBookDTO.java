package cbo.risk.sms.dtos;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class RequestCheckBookDTO extends BaseRequestDTO {


    private Long id;
    private String serialNum;
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
    private Long checkBookId;

    private String startSerialNumber;
    @NotBlank
    @Column(name ="ACCOUNT_NUMBER")
    private String accountNumber;
    private String endSerialNumber;
    private CheckBookType checkBookType;
    private int checkBookLeaveType;



}

