package cbo.risk.sms.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class BaseStockDTO {
    private Long id;
    private LocalDateTime receivedDate;
    private LocalDateTime issuedDate;
    private LocalDateTime returnedDate;
    private int numOfPad;
    private String branchId;
    private String subProcessId;
    private String processId;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime createdTimestamp;
    private LocalDateTime modifiedTimestamp;
}