package cbo.risk.sms.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class RequestCheckBookDTO extends BaseRequestDTO {}

