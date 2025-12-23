package cbo.risk.sms.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseRequestDTO {
    private Long id;
    private String serialNum;
    private String branchId;
    private String subProcessId;
    private String processId;
    private String createdBy;
    private String lastUpdatedBy;
    private LocalDateTime createdTimestamp;
    private LocalDateTime modifiedTimestamp;
}
