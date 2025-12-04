package cbo.risk.sms.models;

import cbo.risk.sms.enums.CheckBookLeaveType;
import cbo.risk.sms.enums.CheckBookType;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
@Entity
@Data
public class Cpo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startingSerialNum;
    private String endSerialNum;

    @NotBlank
    @Column(nullable = false)
    private int numOfBook;
}
