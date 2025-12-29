package cbo.risk.sms.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

@Data
public class BatchResponseDTO {
    private Long parentId;
    private String bookType;
    private String startSerial;
    private String endSerial;
    private int numOfPad;
    private int used;
    private boolean finished;
    private int available;
    private String message;
    private int childrenCreated;
    private String parentBookType;

    private String branchId;


    private String subProcessId;


    private String processId;

    private String createdBy;

    private String createdById;



    private String lastUpdatedBy;

    private String lastUpdatedById;


}
