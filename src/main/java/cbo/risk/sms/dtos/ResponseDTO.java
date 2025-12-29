package cbo.risk.sms.dtos;

import lombok.Data;

@Data
public class ResponseDTO<T>{
    private String message;
    private T result;
    private boolean status;
}
