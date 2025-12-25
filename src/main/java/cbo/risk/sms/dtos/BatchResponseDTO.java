package cbo.risk.sms.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class BatchResponseDTO {
    private Long parentId;
    private String bookType;
    private String startSerial;
    private String endSerial;
    private int numOfPad;
    private int used;
    private int available;
    private String message;
    private int childrenCreated;
    private String parentBookType;


}
