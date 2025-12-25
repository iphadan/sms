package cbo.risk.sms.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
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
